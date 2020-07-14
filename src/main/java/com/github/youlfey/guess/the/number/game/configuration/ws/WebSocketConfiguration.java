package com.github.youlfey.guess.the.number.game.configuration.ws;

import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import com.github.youlfey.guess.the.number.game.service.enrich.SessionEnrichment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

@EnableWebSocketMessageBroker
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private static final String wsEndpoint = "/ws-lab3pt";

    private final SessionEnrichment sessionEnrichment;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint(wsEndpoint)
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler handler, Map<String, Object> attributes) {
                        if (request instanceof ServletServerHttpRequest) {
                            HttpSession session = ((ServletServerHttpRequest) request).getServletRequest().getSession();
                            HttpHeaders headers = request.getHeaders();
                            sessionEnrichment.enrichSessionWithCurrentContract(session, headers);
                            attributes.put(ContractPlayerIdWithJSessionId.key, session.getId());
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                               ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

                    }
                })
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
    }
}
