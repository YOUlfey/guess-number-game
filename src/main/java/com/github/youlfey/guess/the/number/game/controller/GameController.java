package com.github.youlfey.guess.the.number.game.controller;

import com.github.youlfey.guess.the.number.game.api.GameApi;
import com.github.youlfey.guess.the.number.game.pojo.request.ApproveRequest;
import com.github.youlfey.guess.the.number.game.pojo.request.GameCreateRequest;
import com.github.youlfey.guess.the.number.game.pojo.request.GuessRequest;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GamesResponseForPlayer;
import com.github.youlfey.guess.the.number.game.repository.PlayerRepository;
import com.github.youlfey.guess.the.number.game.service.SubscriberNotificationService;
import com.github.youlfey.guess.the.number.game.service.enrich.SessionEnrichment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameApi gameApi;
    private final PlayerRepository playerRepository;
    private final SubscriberNotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity createGame(@RequestBody @Valid GameCreateRequest gameCreateRequest) {
        gameApi.createGame(gameCreateRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/get/{page}")
    @Deprecated
    public ResponseEntity<GamesResponseForPlayer> getMyGames(@PathVariable(required = false) Integer page,
                                                             @RequestParam(required = false, defaultValue = "100") Integer size) {
        Pageable pageable = PageRequest.of(Optional.ofNullable(page).orElse(0), size);
        GamesResponseForPlayer response = gameApi.getGamesResponseForCurrentPlayer(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}/play")
    public ResponseEntity playGame(@PathVariable UUID gameId) {
        gameApi.playGame(gameId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{gameId}/guess")
    public ResponseEntity guessRequest(@PathVariable UUID gameId,
                                       @RequestBody @Valid GuessRequest guessRequest) {
        gameApi.guess(gameId, guessRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{gameId}/approve")
    public ResponseEntity approveRequest(@PathVariable UUID gameId,
                                       @RequestBody @Valid ApproveRequest approveRequest) {
        gameApi.approve(gameId, approveRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-user")
    public ResponseEntity changeUser(@RequestHeader(name = SessionEnrichment.HEADER_USER_NAME) UUID playerId) {
        if (playerRepository.existsById(playerId)) {
            notificationService.notifySpecifiedPlayerWithGamesResponseAsync(playerId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    @GetMapping("/{gameId}/get")
    @Deprecated
    public ResponseEntity<GameResponseForPlayer> getGameResponse(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameApi.getGameResponseForCurrentPlayer(gameId));
    }
}
