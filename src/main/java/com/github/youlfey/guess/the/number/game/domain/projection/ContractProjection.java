package com.github.youlfey.guess.the.number.game.domain.projection;

import org.springframework.beans.factory.annotation.Value;

public interface ContractProjection {

    @Value("#{target.id}")
    String getJSessionId();
}
