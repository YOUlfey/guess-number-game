package com.github.youlfey.guess.the.number.game.api;

import com.github.youlfey.guess.the.number.game.domain.*;
import com.github.youlfey.guess.the.number.game.exception.ErrorFactory;
import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import com.github.youlfey.guess.the.number.game.pojo.request.ApproveRequest;
import com.github.youlfey.guess.the.number.game.pojo.request.GameCreateRequest;
import com.github.youlfey.guess.the.number.game.pojo.request.GuessRequest;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GamesResponseForPlayer;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.service.AsyncExecutor;
import com.github.youlfey.guess.the.number.game.service.ResponseCreatorService;
import com.github.youlfey.guess.the.number.game.service.SubscriberNotificationService;
import com.github.youlfey.guess.the.number.game.service.assistant.CurrentHttpSessionAssistant;
import com.github.youlfey.guess.the.number.game.service.cleaner.GameCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.configuration.GameConfiguration.OPTIMISTIC_RETRY;
import static com.github.youlfey.guess.the.number.game.exception.ErrorFactory.*;

@RequiredArgsConstructor
@Service
public class GameApi {

    private final GameRepository gameRepository;
    private final GamePartyRepository partyRepository;
    private final ActionRecordRepository recordRepository;
    private final CurrentHttpSessionAssistant sessionAssistant;
    private final GameCleaner cleanerService;
    private final ResponseCreatorService creatorService;
    @Qualifier(OPTIMISTIC_RETRY)
    private final RetryTemplate retryTemplate;
    private final SubscriberNotificationService notificationService;
    private final AsyncExecutor asyncExecutor;

    /**
     * Create new GameInstance from GameCreateRequest
     * Scheduling remove gameInstance via GameCleanerService
     *
     * @param createRequest pojo which contains all needed fields to create GameInstance
     * @see GameInstance created game
     * @see GameCreateRequest param of this method
     * @see GameCleaner#scheduleGame(GameInstance) this method is scheduled to remove game by expire
     */
    public void createGame(GameCreateRequest createRequest) {
        Long expired = Optional.ofNullable(createRequest.getExpired()).orElse(GameInstance.defaultExpired);
        PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
        asyncExecutor.execute(() -> {
            GameInstance draftGame = GameInstance
                    .builder()
                    .name(createRequest.getName())
                    .number(createRequest.getNumber())
                    .state(GameState.CREATED)
                    .owner(currentPlayer)
                    .expired(expired)
                    .build();
            GameInstance createdGame = gameRepository.save(draftGame);
            cleanerService.scheduleGame(createdGame);
            notificationService.notifyAllSubscribersForGamesEndpoint();
        });
    }

    /**
     * Start game for current user
     * Method get current player and create GameParty for current game and current player
     *
     * @param gameId selected game
     * @see GameParty
     * @see CurrentHttpSessionAssistant#getCurrentContract()
     */
    public void playGame(UUID gameId) {
        retryTemplate.execute(ctx -> {
            validatePlayGame(gameId);
            PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
            GameInstance game = getGame(gameId);
            if (GameState.RUNNING.equals(game.getState())) {
                throw gameAlreadyIsRunning(gameId);
            }
            game.setState(GameState.RUNNING);
            GameInstance savedGame = gameRepository.save(game);
            partyRepository.save(GameParty.builder().game(savedGame).player(currentPlayer).build());
            notificationService.notifyOwnerThatGameHasStartedAsync(game);
            notificationService.notifyAllSubscribersForGamesEndpointAsync();
            return null;
        });
    }

    public GamesResponseForPlayer getGamesResponseForCurrentPlayer(Pageable pageable) {
        PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
        return creatorService.getGamesResponseForPlayer(pageable, currentPlayer);
    }

    public GameResponseForPlayer getGameResponseForCurrentPlayer(UUID gameId) {
        PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
        return creatorService.getGameResponseForPlayer(gameId, currentPlayer);
    }

    /**
     * Check that current player is not owner of this game, owner can not play in his game
     * Then check the game is not ended or running, because player can not play in game which already running or ended
     *
     * @param gameId simpSessionId of game
     */
    private void validatePlayGame(UUID gameId) {
        PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
        GameInstance game = gameRepository.mustFindById(gameId);
        if (currentPlayer.getId().equals(game.getOwner().getId())) {
            throw cannotPlayInCreatedGame(gameId);
        }
        if (GameState.RUNNING.equals(game.getState()) || GameState.ENDED.equals(game.getState())) {
            throw cannotPlayInRunningOrEndedGame(gameId);
        }
    }

    private GameInstance getGame(UUID gameId) {
        return gameRepository.mustFindById(gameId);
    }

    public void guess(UUID gameId, GuessRequest guessRequest) {
        GameParty party = partyRepository.mustFindByGameId(gameId);
        GameInstance game = party.getGame();
        guessRequest.setGameId(gameId);
        checkSameUser(party.getPlayer());
        Optional<GameActionRecord> recordOpt = recordRepository.findFirstByPartyOrderByDateUpdatedDesc(party);
        if (recordOpt.isPresent()) {
            GameActionRecord actionRecord = recordOpt.get();
            if (actionRecord.isNotApproved()) {
                throw cannotGuessNumberIfNotApprovedYet(guessRequest.getNumber(), gameId);
            }
            if (actionRecord.isResult()) {
                throw noActionForEndedGame(gameId);
            }
        }
        GameActionRecord draft = GameActionRecord.builder()
                .approved(false)
                .party(party)
                .result(false)
                .tryNumber(guessRequest.getNumber())
                .action(PartyAction.GUESS_NUMBER)
                .build();
        if (game.getNumber().equals(guessRequest.getNumber())) {
            completeGame(game, draft);
        }
        recordRepository.save(draft);
        notificationService.notifyAllSubscriptionsForGameEndpointAsync(game);
    }

    private void completeGame(GameInstance game, GameActionRecord actionRecord) {
        game.setState(GameState.ENDED);
        gameRepository.save(game);
        notificationService.notifyAllSubscribersForGamesEndpointByCurrentGameAsync(game);
        notificationService.notifyCompleteGameAsync(game);
        actionRecord.setResult(true);
    }

    public void approve(UUID gameId, ApproveRequest approveRequest) {
        GameParty party = partyRepository.mustFindByGameId(gameId);
        approveRequest.setGameId(gameId);

        checkSameUser(party.getGame().getOwner());

        GameActionRecord actionRecord = recordRepository.findFirstByPartyOrderByDateUpdatedDesc(party)
                .orElseThrow(ErrorFactory::approveIsNotAvailableBeforeGuess);

        if (actionRecord.isResult()) {
            throw noActionForEndedGame(gameId);
        }
        if (actionRecord.isApproved()) {
            throw approveIsNotAvailableIfAlreadyApproved();
        }

        actionRecord.setApproved(true);
        actionRecord.setAction(approveRequest.getAction());
        recordRepository.save(actionRecord);

        notificationService.notifyAllSubscriptionsForGameEndpointAsync(party.getGame());
    }

    private void checkSameUser(PlayerInstance player) {
        PlayerInstance currentPlayer = sessionAssistant.getCurrentPlayer();
        if (!currentPlayer.getId().equals(player.getId())) {
            throw actionIsNotAvailableForUser();
        }
    }
}
