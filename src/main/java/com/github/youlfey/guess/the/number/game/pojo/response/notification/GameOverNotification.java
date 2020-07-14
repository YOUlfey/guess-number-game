package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.NotificationType;

import static com.github.youlfey.guess.the.number.game.pojo.NotificationType.INFO;

public class GameOverNotification extends NotificationResponse {
    private final static NotificationType notificationType = INFO;
    private final static String format = "\"%s\" game over";

    public GameOverNotification(String gameName) {
        super(notificationType, String.format(format, gameName));
    }
}
