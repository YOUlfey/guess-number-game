package com.github.youlfey.guess.the.number.game.exception.internal;

import org.springframework.dao.OptimisticLockingFailureException;

public class GameNotEndedButNumberGuessed extends OptimisticLockingFailureException {
    public GameNotEndedButNumberGuessed() {
        super("The game is not over yet, but the number is guessed");
    }
}
