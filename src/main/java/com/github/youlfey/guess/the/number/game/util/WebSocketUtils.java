package com.github.youlfey.guess.the.number.game.util;

import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;

import java.util.Objects;
import java.util.function.Function;

public class WebSocketUtils {
    private static final Function<StompHeaderAccessor, String> idProvider = accessor -> accessor.getSessionId();
    private static final Function<StompHeaderAccessor, String> destProvider = accessor -> accessor.getDestination();
    private static final Function<StompHeaderAccessor, String> jSessionProvider = accessor -> String.valueOf(
            Objects.requireNonNull(accessor.getSessionAttributes()).get(ContractPlayerIdWithJSessionId.key)
    );

    public static String getSessionIdFromEvent(AbstractSubProtocolEvent event) {
        return getParameterFromEvent(event, idProvider);
    }

    public static String getJSessionId(AbstractSubProtocolEvent event) {
        return getParameterFromEvent(event, jSessionProvider);
    }

    public static String getSimpDestinationFromEvent(AbstractSubProtocolEvent event) {
        return getParameterFromEvent(event, destProvider);
    }

    private static String getParameterFromEvent(AbstractSubProtocolEvent event, Function<StompHeaderAccessor, String> provider) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(event.getMessage());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Objects.requireNonNull(accessor);
        String value = provider.apply(accessor);
        Objects.requireNonNull(value);
        return value;
    }
}
