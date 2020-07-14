package com.github.youlfey.guess.the.number.game.controller.internal.impl;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.controller.internal.InternalController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/games")
public class GameInternalController extends InternalController<GameInstance, UUID> {
    public GameInternalController(GameRepository repository) {
        super(repository);
    }
}
