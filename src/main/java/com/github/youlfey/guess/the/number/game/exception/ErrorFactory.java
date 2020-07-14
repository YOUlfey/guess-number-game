package com.github.youlfey.guess.the.number.game.exception;

import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.exception.ErrorType.*;

public class ErrorFactory {

    public static GameError gameNotFound(UUID gameId) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        return new GameError(notFound, gameNotFound, gameId);
    }

    public static GameError gameAlreadyIsRunning(UUID gameId) {
        HttpStatus methodNotAllowed = HttpStatus.METHOD_NOT_ALLOWED;
        return new GameError(methodNotAllowed, gameAlreadyRunning, gameId);
    }

    public static GameError cannotPlayInCreatedGame(UUID gameId) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return new GameError(conflict, cannotPlayInCreatedGame, gameId);
    }

    public static GameError cannotPlayInRunningOrEndedGame(UUID gameId) {
        HttpStatus methodNotAllowed = HttpStatus.METHOD_NOT_ALLOWED;
        return new GameError(methodNotAllowed, cannotPlayInEndedOrRunningGame, gameId);
    }

    public static GameError currentActionIsNotExist(String keyAction, Collection<String> actions) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return new GameError(conflict, partyActionNotExist, keyAction, actions);
    }

    public static GameError partyNotFound(UUID gameId) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        return new GameError(notFound, partyNotFoundByGame, gameId);
    }

    public static GameError cannotGuessNumberIfNotApprovedYet(Integer number, UUID gameId) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return new GameError(conflict, cannotGuessIfNotApprovePrev, gameId, number);
    }

    public static GameError noActionForEndedGame(UUID gameId) {
        HttpStatus forbidden = HttpStatus.FORBIDDEN;
        return new GameError(forbidden, cannotMoveIfGameAlreadyEnded, gameId);
    }

    public static GameError approveIsNotAvailableBeforeGuess() {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return new GameError(conflict, approveIsNotAvailableBeforeGuess);
    }

    public static GameError approveIsNotAvailableIfAlreadyApproved() {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return new GameError(conflict, approveIsNotAvailableIfAlreadyApproved);
    }

    public static GameError actionIsNotAvailableForUser() {
        HttpStatus methodNotAllowed = HttpStatus.METHOD_NOT_ALLOWED;
        return new GameError(methodNotAllowed, actionIsNotAvailableForPlayer);
    }

    public static GameError playerIsTheOwnerOfTheGame(UUID gameId) {
        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        return new GameError(internalServerError, playerIsTheOwnerOfTheGame, gameId);
    }

    public static GameError notMemberForGame(UUID gameId) {
        HttpStatus methodNotAllowed = HttpStatus.METHOD_NOT_ALLOWED;
        return new GameError(methodNotAllowed, notMemberForGame, gameId);
    }



}
