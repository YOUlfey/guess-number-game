package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.NotificationType;

import static com.github.youlfey.guess.the.number.game.pojo.NotificationType.INFO;

public class GameHasStartedForOwnerNotification extends NotificationResponse {
    private final static NotificationType notificationType = INFO;
    private final static String format = "Your game \"%s\" is started...";

    public GameHasStartedForOwnerNotification(String gameName) {
        super(notificationType, String.format(format, gameName));
    }
}
