package com.github.youlfey.guess.the.number.game.listener.ws;

import com.github.youlfey.guess.the.number.game.configuration.ws.Destination;
import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GameResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.content.GamesResponseForPlayer;
import com.github.youlfey.guess.the.number.game.pojo.response.error.InternalErrorResponse;
import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.service.ResponseCreatorService;
import com.github.youlfey.guess.the.number.game.util.WebSocketUtils;
import com.github.youlfey.guess.the.number.game.service.assistant.WebSocketSessionAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketListener {
    private final WebSocketSessionAssistant webSocketSessionAssistant;
    private final ResponseCreatorService creatorService;
    private final SimpMessagingTemplate template;
    private final ContractRepository contractRepository;

    @EventListener(SessionConnectedEvent.class)
    public void handleWsConnectedEvent(SessionConnectedEvent event) {
        String simpSessionId = WebSocketUtils.getSessionIdFromEvent(event);
        log.info("Connected event, simpSessionId {}", simpSessionId);
        log.debug("Connected event {}", event);
    }

    @EventListener(SessionConnectEvent.class)
    public void handleWsConnectEvent(SessionConnectEvent event) {
        String simpSessionId = WebSocketUtils.getSessionIdFromEvent(event);
        log.info("Connect event, simpSessionId {}", simpSessionId);
        log.debug("Connect event {}", event);
    }

    @EventListener(SessionSubscribeEvent.class)
    public void handleWsSubscribeEvent(SessionSubscribeEvent event) {
        ContractPlayerIdWithJSessionId currentContract = getContract(event);
        String simpSessionId = WebSocketUtils.getSessionIdFromEvent(event);
        String simpDestination = WebSocketUtils.getSimpDestinationFromEvent(event);
        String jSessionId = currentContract.getId();
        log.info("Subscribe event, simpSessionId {}, simpDestination {}", simpSessionId, simpDestination);
        log.debug("Subscribe event {}", event);
        webSocketSessionAssistant.persist(simpDestination, jSessionId, simpSessionId);

        PlayerInstance currentPlayer = currentContract.getPlayer();
        sendResponseInformationAfterSubscribe(simpSessionId, simpDestination, currentPlayer);

        log.info("Current subscription persisted and mapped to JSESSIONID {}", jSessionId);
    }

    private void sendResponseInformationAfterSubscribe(String simpSessionId, String simpDestination, PlayerInstance currentPlayer) {
        try {
            if (Destination.Utils.isSpecifyGameDestination(simpDestination)) {
                Optional<UUID> gameId = Destination.Utils.getGameIdFromEndpoint(simpDestination);
                if (!gameId.isPresent()) {
                    throw new RuntimeException("Game id " + Destination.Utils.getIdAsStrFromEndpoint(simpDestination) + " is not correct");
                }
                String sendDest = Destination.Utils.convertSubscribeDestToSendDest(simpDestination);
                gameId.ifPresent(id -> {
                    GameResponseForPlayer response = creatorService.getGameResponseForPlayer(id, currentPlayer);
                    template.convertAndSendToUser(simpSessionId, sendDest, response);
                });
            } else if (Destination.Utils.isGamesDestination(simpDestination)) {
                GamesResponseForPlayer response = creatorService.getGamesResponseForPlayer(currentPlayer);
                String sendDest = Destination.Utils.convertSubscribeDestToSendDest(simpDestination);
                template.convertAndSendToUser(simpSessionId, sendDest, response);
            } else {
                throw new RuntimeException("This destination is not supported");
            }
        } catch (Exception e) {
            Response response = new InternalErrorResponse(e.getMessage());
            String sendDest = Destination.Utils.convertSubscribeDestToSendDest(simpDestination);
            template.convertAndSendToUser(simpSessionId, sendDest, response);
            webSocketSessionAssistant.removeBySimpSessionId(simpSessionId);
        }
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWsDisconnectEvent(SessionDisconnectEvent event) {
        String simpSessionId = WebSocketUtils.getSessionIdFromEvent(event);
        log.info("Disconnect event, simpSessionId {}", simpSessionId);
        log.debug("Disconnect event {}", event);
        webSocketSessionAssistant.removeBySimpSessionId(simpSessionId);
    }

    @EventListener(SessionUnsubscribeEvent.class)
    public void handleWsUnSubscribeEvent(SessionUnsubscribeEvent event) {
        String simpSessionId = WebSocketUtils.getSessionIdFromEvent(event);
        log.info("Unsubscribe event, simpSessionId {}", simpSessionId);
        log.debug("Unsubscribe event {}", event);
        webSocketSessionAssistant.removeBySimpSessionId(simpSessionId);
    }

    private ContractPlayerIdWithJSessionId getContract(AbstractSubProtocolEvent event) {
        String jSessionId = WebSocketUtils.getJSessionId(event);
        return contractRepository.mustFindById(jSessionId);
    }

    private PlayerInstance getPlayer(AbstractSubProtocolEvent event) {
        return getContract(event).getPlayer();
    }

}
