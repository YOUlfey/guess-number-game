package com.github.youlfey.guess.the.number.game.pojo.callback;

import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.function.Supplier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyCallback {
    private Set<NotifySet> notifySets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifySet {
        private String simpSessionId;
        private String destination;
        private Supplier<Response> responseProvider;
    }
}
