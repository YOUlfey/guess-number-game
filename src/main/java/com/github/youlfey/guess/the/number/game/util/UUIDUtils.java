package com.github.youlfey.guess.the.number.game.util;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class UUIDUtils {

    /**
     * Метод проверяет можно ли спарсить String в UUID
     * @param playerId айдишник текущего игрока
     * @return boolean
     */
    public static boolean isValidUUID(String playerId) {
        try {
            UUID uuid = UUID.fromString(playerId);
            return true;
        } catch (IllegalArgumentException e) {
            log.info("Invalid simpSessionId {}, error {}", playerId, e.getMessage());
        }
        return false;
    }
}
