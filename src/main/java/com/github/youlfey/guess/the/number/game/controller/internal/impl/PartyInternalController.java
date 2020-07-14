package com.github.youlfey.guess.the.number.game.controller.internal.impl;

import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.controller.internal.InternalController;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/parties")
public class PartyInternalController extends InternalController<GameParty, UUID> {

    public PartyInternalController(GamePartyRepository repository) {
        super(repository);
    }
}
