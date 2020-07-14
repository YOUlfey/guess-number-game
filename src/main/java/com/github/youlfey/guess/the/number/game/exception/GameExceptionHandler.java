package com.github.youlfey.guess.the.number.game.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GameExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GameError.class)
    public final ResponseEntity handleGameErrors(GameError error) {
        return ResponseEntity.status(error.getStatus()).body(error);
    }
}
