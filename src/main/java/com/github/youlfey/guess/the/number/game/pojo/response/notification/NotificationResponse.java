package com.github.youlfey.guess.the.number.game.pojo.response.notification;

import com.github.youlfey.guess.the.number.game.pojo.ResponseType;
import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import com.github.youlfey.guess.the.number.game.pojo.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NotificationResponse implements Response {
    private final ResponseType type = ResponseType.NOTIFICATION;
    private final NotificationType notificationType;
    private final String message;
}
