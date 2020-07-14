package com.github.youlfey.guess.the.number.game.controller.internal.impl;

import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.repository.PlayerRepository;
import com.github.youlfey.guess.the.number.game.controller.internal.InternalController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/players")
public class PlayerInternalController extends InternalController<PlayerInstance, UUID> {

    public PlayerInternalController(PlayerRepository repository) {
        super(repository);
    }
}
