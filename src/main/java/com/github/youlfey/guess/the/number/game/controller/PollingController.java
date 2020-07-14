package com.github.youlfey.guess.the.number.game.controller;

import com.github.youlfey.guess.the.number.game.api.GameApi;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GamesResponseForPlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/polling")
@RequiredArgsConstructor
public class PollingController {
    private final GameApi api;

    @GetMapping("/games")
    public ResponseEntity<GamesResponseForPlayer> getGames() {
        return ResponseEntity.ok(api.getGamesResponseForCurrentPlayer(PageRequest.of(0, 100)));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<GameResponseForPlayer> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(api.getGameResponseForCurrentPlayer(gameId));
    }
}
