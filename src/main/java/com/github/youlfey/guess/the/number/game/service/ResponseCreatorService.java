package com.github.youlfey.guess.the.number.game.service;

import com.github.youlfey.guess.the.number.game.configuration.GameConfiguration;
import com.github.youlfey.guess.the.number.game.domain.*;
import com.github.youlfey.guess.the.number.game.exception.internal.GameNotEndedButNumberGuessed;
import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GamesResponseForPlayer;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.service.assistant.CurrentHttpSessionAssistant;
import com.github.youlfey.guess.the.number.game.util.DefaultValues;
import com.github.youlfey.guess.the.number.game.util.RandomUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.github.youlfey.guess.the.number.game.exception.ErrorFactory.notMemberForGame;
import static com.github.youlfey.guess.the.number.game.exception.ErrorFactory.playerIsTheOwnerOfTheGame;

@Service
@RequiredArgsConstructor
public class ResponseCreatorService {

    public static final String GAME_ALREADY_ENDED = "Game already ended";
    public static final String LAST_ACTION_ALREADY_APPROVED = "Last action already approved";
    public static final String GAME_IS_NOT_APPROVED_YET = "Game is not approved yet";
    private final GameRepository gameRepository;
    private final GamePartyRepository partyRepository;
    private final ActionRecordRepository recordRepository;
    @Qualifier(GameConfiguration.OPTIMISTIC_RETRY)
    private final RetryTemplate retryTemplate;

    /**
     * Get games where current player is owner
     *
     * @param pageable pagination parameters (page number, size)
     * @return Page with games
     * @see GameRepository#findAllByOwnerAndStateIsIn(Pageable, PlayerInstance, Collection) this method find all games
     * for current player, where this player is owner and where game states is specified in the collection
     */
    private Page<GameInstance> getGamesByOwner(Pageable pageable, PlayerInstance owner) {
        return gameRepository.findAllByOwnerAndStateIsIn(pageable, owner, ImmutableList.of(GameState.CREATED, GameState.RUNNING));
    }

    /**
     * Get available games for playing
     *
     * @param pageable pagination parameters (page number, size)
     * @return Page with games
     * @see GameRepository#findAllByOwnerNotAndStateIsIn(Pageable, PlayerInstance, Collection) this method find all games
     * for current player, where this player is not owner and where game states is specified in the collection
     */
    private Page<GameInstance> getGamesNotForOwner(Pageable pageable, PlayerInstance owner) {
        return gameRepository.findAllByOwnerNotAndStateIsIn(pageable, owner, ImmutableList.of(GameState.CREATED));
    }

    /**
     * Get games where current player is playing
     *
     * @param pageable pagination parameters (page number, size)
     * @return Page with games
     * @see GamePartyRepository#findAllByPlayerAndGame_StateIsIn(Pageable, PlayerInstance, Collection) this method find all parties
     * for current player where game states is specified in the collection
     */
    private Page<GameInstance> getGamesWherePlayerAct(Pageable pageable, PlayerInstance player) {
        return partyRepository.findAllByPlayerAndGame_StateIsIn(pageable, player, ImmutableList.of(GameState.RUNNING))
                .map(GameParty::getGame);
    }

    /**
     * Create response for current Player, which includes available games, created games, current playing games
     *
     * @param pageable pagination parameters (page number, size)
     * @return response for current player
     * @see CurrentHttpSessionAssistant response
     */
    public GamesResponseForPlayer getGamesResponseForPlayer(Pageable pageable, PlayerInstance player) {
        Page<GameInstance> gamesForOwner = getGamesByOwner(pageable, player);
        Page<GameInstance> gamesNotForOwner = getGamesNotForOwner(pageable, player);
        Page<GameInstance> gamesForPlayer = getGamesWherePlayerAct(pageable, player);
        return new GamesResponseForPlayer(player, gamesForOwner, gamesNotForOwner, gamesForPlayer);
    }

    public GamesResponseForPlayer getGamesResponseForPlayer(PlayerInstance player) {
        return getGamesResponseForPlayer(DefaultValues.DEFAULT_PAGE, player);
    }

    public GameResponseForPlayer getGameResponseForPlayer(UUID gameId, PlayerInstance player) {
        GameInstance game = gameRepository.mustFindById(gameId);
        return retryTemplate.execute(ctx -> getGameResponseForPlayerInternal(game, player));
    }

    GameResponseForPlayer retryGetGameResponseForPlayer(GameInstance game, PlayerInstance player) {
        return retryTemplate.execute(ctx -> getGameResponseForPlayerInternal(game, player));
    }

    private GameResponseForPlayer getGameResponseForPlayerInternal(GameInstance game, PlayerInstance player) {
        GameParty party = partyRepository.mustFindByGame(game);
        boolean isOwner = player.getId().equals(game.getOwner().getId());
        boolean isPlayer = player.getId().equals(party.getPlayer().getId());
        if (isOwner && isPlayer) {
            throw playerIsTheOwnerOfTheGame(game.getId()); // владелец не может играть в свою игру
        }

        Optional<GameActionRecord> lastActionRecordOpt = recordRepository.findFirstByPartyOrderByDateUpdatedDesc(party);

        if (isOwner) {
            if (lastActionRecordOpt.isPresent()) {
                GameActionRecord record = lastActionRecordOpt.get();
                if (record.isResult()) {
                    return new GameResponseForPlayer(player, game, true, Collections.emptyList(), GAME_ALREADY_ENDED);
                } else if (record.isApproved()) {
                    return new GameResponseForPlayer(player, game, false, Collections.emptyList(), LAST_ACTION_ALREADY_APPROVED);
                } else {

                    if (Objects.equals(game.getNumber(), record.getTryNumber())) {
                        throw new GameNotEndedButNumberGuessed();
                    }

                    long countApproved = recordRepository.countGameActionRecordByApprovedTrue();

                    if (!record.isCanLie() && RandomUtils.random() < countApproved) {
                        recordRepository.deleteAllByApprovedTrue();
                        record.setCanLie(true);
                        recordRepository.save(record);
                    }

                    String content = "Check following player number: " + record.getTryNumber();

                    if (record.isCanLie()) {
                        return new GameResponseForPlayer(player, game, false, ImmutableSet.of(PartyAction.LESS_NUMBER, PartyAction.MORE_NUMBER), content);
                    }

                    Collection<PartyAction> availableActions = record.getTryNumber() > game.getNumber() ?
                            Collections.singleton(PartyAction.LESS_NUMBER) :
                            Collections.singleton(PartyAction.MORE_NUMBER);

                    return new GameResponseForPlayer(player, game, false, availableActions, content);
                }
            } else {
                return new GameResponseForPlayer(player, game, false, Collections.emptyList());
            }
        }

        if (isPlayer) {
            if (lastActionRecordOpt.isPresent()) {
                GameActionRecord record = lastActionRecordOpt.get();
                if (record.isResult()) {
                    return new GameResponseForPlayer(player, game, true, Collections.emptyList(), GAME_ALREADY_ENDED);
                } else if (record.isNotApproved()) {
                    return new GameResponseForPlayer(player, game, false, Collections.emptyList(), GAME_IS_NOT_APPROVED_YET);
                } else {
                    String content = null;
                    if (record.getAction().equals(PartyAction.LESS_NUMBER)) {
                        content = "The owner answer that the number is less than " + record.getTryNumber();
                    }
                    if (record.getAction().equals(PartyAction.MORE_NUMBER)) {
                        content = "The owner answer that the number is greater than " + record.getTryNumber();
                    }
                    if (StringUtils.isEmpty(content)) {
                        throw new NullPointerException();
                    }
                    return new GameResponseForPlayer(player, game, false, Collections.singleton(PartyAction.GUESS_NUMBER), content);
                }
            } else {
                return new GameResponseForPlayer(player, game, false, Collections.singleton(PartyAction.GUESS_NUMBER), "Guess a first number");
            }
        }

        throw notMemberForGame(game.getId());
    }
}
