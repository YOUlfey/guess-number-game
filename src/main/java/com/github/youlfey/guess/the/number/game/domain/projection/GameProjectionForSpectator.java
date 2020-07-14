package com.github.youlfey.guess.the.number.game.domain.projection;

import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.UUID;

@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class GameProjectionForSpectator {
    private final UUID gameId;
    private final String gameName;
    private final UUID partyId;
    private final Integer guessNumber;
    private final Boolean isApproved;
    private final Boolean isComplete;
    private final PartyAction lastAction;

    public GameProjectionForSpectator(GameActionRecord lastRecord) {
        GameParty party = lastRecord.getParty();
        GameInstance game = party.getGame();
        this.gameId = game.getId();
        this.gameName = game.getName();
        this.partyId = party.getId();
        this.guessNumber = lastRecord.getTryNumber();
        this.isApproved = lastRecord.isApproved();
        this.isComplete = lastRecord.isResult();
        this.lastAction = lastRecord.getAction();
    }

    public GameProjectionForSpectator(GameParty party) {
        GameInstance game = party.getGame();
        this.gameId = game.getId();
        this.gameName = game.getName();
        this.partyId = party.getId();
        this.isComplete = false;
        this.isApproved = null;
        this.guessNumber = null;
        this.lastAction = null;
    }
}
