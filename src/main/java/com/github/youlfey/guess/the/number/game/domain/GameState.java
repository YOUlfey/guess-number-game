package com.github.youlfey.guess.the.number.game.domain;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum  GameState {
    CREATED, RUNNING, ENDED;

    public List<GameState> getAvailableStates() {
        return Stream.of(CREATED, RUNNING, ENDED).collect(toList());
    }
}
