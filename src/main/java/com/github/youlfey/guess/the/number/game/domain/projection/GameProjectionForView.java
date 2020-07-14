package com.github.youlfey.guess.the.number.game.domain.projection;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameState;
import com.github.youlfey.guess.the.number.game.util.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GameProjectionForView {
    private UUID id;
    private String name;
    private Long createdDate;
    private GameState state;

    public GameProjectionForView(GameInstance gameInstance) {
        this.id = gameInstance.getId();
        this.name = gameInstance.getName();
        this.createdDate = DateUtils.toEpochMilli(gameInstance.getCreatedDate());
        this.state = gameInstance.getState();
    }
}
