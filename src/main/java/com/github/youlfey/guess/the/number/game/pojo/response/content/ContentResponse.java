package com.github.youlfey.guess.the.number.game.pojo.response.content;

import com.github.youlfey.guess.the.number.game.pojo.ResponseType;
import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.pojo.ResponseType.CONTENT;

@Getter
@RequiredArgsConstructor
public class ContentResponse implements Response {
    private final ResponseType type = CONTENT;
    private final UUID playerId;
}
