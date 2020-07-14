package com.github.youlfey.guess.the.number.game.controller;

import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import com.github.youlfey.guess.the.number.game.domain.projection.GameProjectionForSpectator;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/spectator")
@RequiredArgsConstructor
public class SpectatorController {
    private final GameRepository gameRepository;
    private final GamePartyRepository partyRepository;
    private final ActionRecordRepository recordRepository;

    @GetMapping("/game/{gameId}")
    public ResponseEntity<GameProjectionForSpectator> getSpectatorResp(@PathVariable UUID gameId) {
        GameInstance gameInstance = gameRepository.mustFindById(gameId);
        GameParty gameParty = partyRepository.mustFindByGame(gameInstance);
        Optional<GameActionRecord> record = recordRepository.findFirstByPartyOrderByDateUpdatedDesc(gameParty);
        return record.map(gameActionRecord -> ResponseEntity.ok(new GameProjectionForSpectator(gameActionRecord)))
                .orElseGet(() -> ResponseEntity.ok(new GameProjectionForSpectator(gameParty)));
    }
}
