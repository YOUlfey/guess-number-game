package com.github.youlfey.guess.the.number.game.pojo;

import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.exception.ErrorFactory;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Enum need to set current action in GameResponse
 * @see GameResponseForPlayer
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PartyAction {
    MORE_NUMBER("more", "Check that number more alleged"),
    LESS_NUMBER("less", "Check that number less alleged"),
    GUESS_NUMBER("guess", "Set a number to guess");

    private static Map<String, PartyAction> approveActions = ImmutableMap.of(
            MORE_NUMBER.key, MORE_NUMBER,
            LESS_NUMBER.key, LESS_NUMBER
    );

    public static PartyAction approveOf(String key) {
        return Optional.ofNullable(approveActions.get(key)).orElseThrow(() -> ErrorFactory.currentActionIsNotExist(key, approveActions.keySet()));
    }

    private final String key;
    private final String value;
}
