package com.github.youlfey.guess.the.number.game.controller.internal.impl;

import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.controller.internal.InternalController;
import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/contracts")
public class ContractInternalController extends InternalController<ContractPlayerIdWithJSessionId, String> {

    public ContractInternalController(ContractRepository repository) {
        super(repository);
    }
}
