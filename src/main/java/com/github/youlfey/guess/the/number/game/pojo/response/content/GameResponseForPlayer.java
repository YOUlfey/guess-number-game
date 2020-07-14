package com.github.youlfey.guess.the.number.game.pojo.response.content;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import com.github.youlfey.guess.the.number.game.util.DateUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class GameResponseForPlayer extends ContentResponse {
    private final UUID gameId;
    private final boolean isComplete;
    private final Long gameCreatedAt;
    private final Long gameUpdatedAt;
    private final Collection<PartyAction> availableActions;
    private String content;

    public GameResponseForPlayer(PlayerInstance player, GameInstance game, boolean isComplete, Collection<PartyAction> availableActions) {
        super(player.getId());
        this.gameId = game.getId();
        this.gameCreatedAt = DateUtils.toEpochMilli(game.getCreatedDate());
        this.gameUpdatedAt = DateUtils.toEpochMilli(game.getUpdatedDate());
        this.isComplete = isComplete;
        this.availableActions = availableActions;
    }

    public GameResponseForPlayer(PlayerInstance player, GameInstance game, boolean isComplete, Collection<PartyAction> availableActions, String content) {
        this(player, game, isComplete, availableActions);
        this.content = content;
    }
}
