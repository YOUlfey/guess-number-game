package com.github.youlfey.guess.the.number.game.pojo.response.error;

import com.github.youlfey.guess.the.number.game.pojo.ResponseType;
import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.github.youlfey.guess.the.number.game.pojo.ResponseType.INTERNAL_ERROR;

@RequiredArgsConstructor
@Getter
public class InternalErrorResponse implements Response {
    private final ResponseType type = INTERNAL_ERROR;
    private final String message;
}
