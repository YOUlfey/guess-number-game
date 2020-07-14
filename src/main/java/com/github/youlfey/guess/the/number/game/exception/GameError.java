package com.github.youlfey.guess.the.number.game.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.Objects;

@Getter
@JsonIgnoreProperties(value = {"cause", "stackTrace", "localizedMessage", "suppressed"})
public class GameError extends RuntimeException {
    public static final String TEXT = "text";
    public static final String VALUE = "value";
    private Long timestamp;
    private String message;
    private HttpStatus status;
    private String code;

    GameError(HttpStatus status, ErrorType errorType, Object... params) {
        if (Objects.isNull(params) || params.length == 0) {
            if (errorType.getCountParams() == 0) {
                this.message = errorType.getMessage();
            } else {
                throw new IllegalArgumentException("Message require params, but it's empty");
            }
        }
        if (params.length == errorType.getCountParams()) {
            this.message = String.format(errorType.getMessage(), params);
        } else {
            throw new IllegalArgumentException("Count of params does not match required");
        }
        this.status = status;
        this.code = errorType.getCode();
        this.timestamp = new Date().getTime();
    }
}
