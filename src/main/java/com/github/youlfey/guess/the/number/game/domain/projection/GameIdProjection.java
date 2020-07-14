package com.github.youlfey.guess.the.number.game.domain.projection;

import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

public interface GameIdProjection {
    @Value("#{target.id}")
    UUID getGameId();
}
