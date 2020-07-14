package com.github.youlfey.guess.the.number.game.pojo.request;

import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class ApproveRequest {

    private final PartyAction action;
    @Setter
    private UUID gameId;

    @JsonCreator
    public ApproveRequest(@JsonProperty("action") String actionKey) {
        this.action = PartyAction.approveOf(actionKey);
    }
}
