package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.NotificationType;
import lombok.Getter;

@Getter
public class DeleteGameNotification extends NotificationResponse {
    private static final NotificationType notificationType = NotificationType.INFO;
    private static final String format = "Your game \"%s\" deleted.";
    private final Boolean deleted = true;

    public DeleteGameNotification(String gameName) {
        super(notificationType, String.format(format, gameName));
    }
}
