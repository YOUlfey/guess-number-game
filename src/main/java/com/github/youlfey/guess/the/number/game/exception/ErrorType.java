package com.github.youlfey.guess.the.number.game.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
class ErrorType {
    private final String code;
    private final String message;
    private final Integer countParams;

    private static String getCode(Integer index) {
        return String.format("LAB_PT-%s", index);
    }

    static ErrorType gameNotFound = new ErrorType(getCode(1), "Game not found by id %s", 1);
    static ErrorType gameAlreadyRunning = new ErrorType(getCode(2), "Game %s already running", 1);
    static ErrorType cannotPlayInCreatedGame = new ErrorType(getCode(3), "You cannot play in your created game %s", 1);
    static ErrorType cannotPlayInEndedOrRunningGame = new ErrorType(getCode(4), "You cannot play in already running or ended game %s", 1);
    static ErrorType partyActionNotExist = new ErrorType(getCode(5), "Current action %s is not exist. Please select one of the following: %s", 2);
    static ErrorType partyNotFoundByGame = new ErrorType(getCode(6), "Party for game %s not found", 1);
    static ErrorType cannotGuessIfNotApprovePrev = new ErrorType(getCode(7), "You cannot guess number %s for game %s if owner does not approve yet previous move.", 2);
    static ErrorType cannotMoveIfGameAlreadyEnded = new ErrorType(getCode(8), "You cannot something do because game %s already ended", 1);
    static ErrorType approveIsNotAvailableBeforeGuess = new ErrorType(getCode(9), "Owner cannot move before player. Another words: player should be set number for your approving", 0);
    static ErrorType approveIsNotAvailableIfAlreadyApproved = new ErrorType(getCode(10), "You can not approve guess number if already it's approved", 0);
    static ErrorType actionIsNotAvailableForPlayer = new ErrorType(getCode(11), "This action unavailable for you", 0);
    static ErrorType playerIsTheOwnerOfTheGame = new ErrorType(getCode(12), "Current player is the owner of the game %s", 1);
    static ErrorType notMemberForGame = new ErrorType(getCode(13), "You are not a member of game %s", 1);
}
