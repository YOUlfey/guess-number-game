package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.NotificationType;

public class GameHasBeenUpdatedNotification extends NotificationResponse {
    private static final NotificationType notificationType = NotificationType.INFO;
    private static final String format = "Your game \"%s\" has been updated";

    public GameHasBeenUpdatedNotification(String gameName) {
        super(notificationType, String.format(format, gameName));
    }
}
