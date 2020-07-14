package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.NotificationType;

public class BeforeDeleteGameNotification extends NotificationResponse {
    private static final NotificationType notificationType = NotificationType.WARN;
    private static final String format = "Your game \"%s\" will be deleted in a minute if you do not complete the action.";

    public BeforeDeleteGameNotification(String gameName) {
        super(notificationType, String.format(format, gameName));
    }
}
