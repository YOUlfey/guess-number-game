package com.github.youlfey.guess.the.number.game.controller.internal.impl;

import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.controller.internal.InternalController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/actions")
public class GameActionInternalController extends InternalController<GameActionRecord, UUID> {

    public GameActionInternalController(ActionRecordRepository repository) {
        super(repository);
    }
}
