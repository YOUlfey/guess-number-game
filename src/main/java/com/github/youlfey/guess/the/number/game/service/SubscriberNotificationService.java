package com.github.youlfey.guess.the.number.game.service;

import com.github.youlfey.guess.the.number.game.configuration.ws.Destination;
import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.exception.GameError;
import com.github.youlfey.guess.the.number.game.pojo.NotificationType;
import com.github.youlfey.guess.the.number.game.pojo.callback.NotifyCallback;
import com.github.youlfey.guess.the.number.game.pojo.response.Response;
import com.github.youlfey.guess.the.number.game.pojo.response.notification.*;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.PlayerRepository;
import com.github.youlfey.guess.the.number.game.service.assistant.WebSocketSessionAssistant;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static com.github.youlfey.guess.the.number.game.wrapper.SimpMessagingTemplateWrapper.TEMPLATE_WRAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriberNotificationService {
    private final WebSocketSessionAssistant webSocketSessionAssistant;
    @Qualifier(value = TEMPLATE_WRAPPER)
    private final SimpMessagingTemplate template;
    private final ResponseCreatorService creatorService;
    private final ContractRepository contractRepository;
    private final GamePartyRepository partyRepository;
    private final ActionRecordRepository recordRepository;
    private final PlayerRepository playerRepository;
    private final AsyncExecutor asyncExecutor;

    public void notifyAllSubscribersForGamesEndpointAsync() {
        asyncExecutor.execute(this::notifyAllSubscribersForGamesEndpoint);
    }

    public void notifyAllSubscribersForGamesEndpoint() {
        log.info("Sending notification for all subscribers for main page with games is started");
        Map<String, String> mappingJSessionToWsSession = webSocketSessionAssistant.
                getMappingJSessionToWsSessionByDestination(Destination.getSubscribeGamesEndpoint().getEndpoint());
        if (MapUtils.isNotEmpty(mappingJSessionToWsSession)) {
            mappingJSessionToWsSession.forEach(
                    (jSessionId, simpSessionId) -> contractRepository.findById(jSessionId).ifPresent(contract -> {
                        PlayerInstance player = contract.getPlayer();
                        Response response = creatorService.getGamesResponseForPlayer(player);
                        template.convertAndSendToUser(simpSessionId, Destination.getSendGamesEndpoint().getEndpoint(), response);
                        log.info("Notification was sent to destination {}, for user {}", Destination.getSendGamesEndpoint().getEndpoint(), player.getId());
                        log.debug("Notifications payload {}", response);
                    })
            );
        }
        log.info("Sending notification for all subscribers for main page with games is complete");
    }

    public void notifySpecifiedPlayerWithGamesResponseAsync(UUID playerId) {
        asyncExecutor.execute(() -> notifySpecifiedPlayerWithGamesResponse(playerId));
    }

    public void notifySpecifiedPlayerWithGamesResponse(UUID playerId) {
        log.info("Sending notification for specified player {} for main page with games is started asynchronously", playerId);
        playerRepository.findById(playerId).ifPresent(playerInstance ->
                contractRepository.getAllByPlayerId(playerId).forEach(contractProjection -> {
                    String jSessionId = contractProjection.getJSessionId();
                    Collection<String> wsSessionIds = webSocketSessionAssistant.getWsSessionIds(Destination.getSubscribeGamesEndpoint().getEndpoint(), jSessionId);
                    Response response = creatorService.getGamesResponseForPlayer(playerInstance);
                    convertAndSendToUsersInternal(wsSessionIds, Destination.getSendGamesEndpoint().getEndpoint(), response);
                    log.info("Notification was sent to destination {}, for user {}", Destination.getSendGamesEndpoint().getEndpoint(), playerId);
                    log.debug("Notifications payload {}", response);
                }));
        log.info("Sending notification for specified player {} for main page with games is complete", playerId);
    }

    public void notifyAllSubscribersForGamesEndpointByCurrentGameAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyAllSubscribersForGamesEndpointByCurrentGame(game));
    }

    public NotifyCallback createCallBackForNotifyAllSubscribersForGamesEndpointByCurrentGame(GameInstance game) {
        log.info("Create notification callback for all subscribers for main page with games is started");
        NotifyCallback callback = NotifyCallback.builder().notifySets(new HashSet<>()).build();
        Set<NotifyCallback.NotifySet> notifySets = callback.getNotifySets();
        Set<PlayerInstance> userIds = new HashSet<PlayerInstance>() {{
            add(game.getOwner());
        }};
        partyRepository.findByGame(game).ifPresent(party -> userIds.add(party.getPlayer()));
        userIds.forEach(player -> contractRepository.getAllByPlayerId(player.getId()).forEach(contractProjection -> {
            String jSessionId = contractProjection.getJSessionId();
            Collection<String> wsSessionIds = webSocketSessionAssistant.getWsSessionIds(Destination.getSubscribeGamesEndpoint().getEndpoint(), jSessionId);
            wsSessionIds.forEach(wsSessionId -> notifySets.add(NotifyCallback.NotifySet.builder()
                    .destination(Destination.getSendGamesEndpoint().getEndpoint())
                    .simpSessionId(wsSessionId)
                    .responseProvider(() -> creatorService.getGamesResponseForPlayer(player))
                    .build()));
        }));
        log.info("Create notification callback for all subscribers for main page with games is complete");
        log.debug("Notification callback {}", callback);
        return callback;
    }

    public void notifyAllSubscribersForGamesEndpointByCurrentGame(GameInstance game) {
        NotifyCallback callback = createCallBackForNotifyAllSubscribersForGamesEndpointByCurrentGame(game);
        convertAndSentWithCallback(callback);
    }

    public void notifyAllSubscriptionsForGameEndpointAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyAllSubscriptionsForGameEndpoint(game));
    }

    public void notifyAllSubscriptionsForGameEndpoint(GameInstance game) {
        partyRepository.findByGame(game).ifPresent(party -> {
            log.info("Sending notification for all players for game {} is started asynchronously", game.getId());
            UUID gameId = game.getId();
            String subscribeDestinationEndpoint = Destination.getSubscribeGameEndpoint(gameId).getEndpoint();
            String sendDestinationEndpoint = Destination.getSendGameEndpoint(gameId).getEndpoint();
            PlayerInstance player = party.getPlayer();
            PlayerInstance owner = game.getOwner();
            Stream.of(player, owner).forEach(playerInstance -> contractRepository.getAllByPlayerId(playerInstance.getId())
                    .forEach(contractProjection -> {
                                String jSessionId = contractProjection.getJSessionId();
                                Collection<String> wsSessionIds = webSocketSessionAssistant.getWsSessionIds(subscribeDestinationEndpoint, jSessionId);
                                if (CollectionUtils.isEmpty(wsSessionIds)) {
                                    log.warn("Web socket sessions is not exist for endpoint {} for player {}", subscribeDestinationEndpoint, playerInstance.getId());
                                    Map<String, Collection<String>> wsSessionIdsWithEndpoint = webSocketSessionAssistant.getWsSessionIdsWithEndpoint(jSessionId);
                                    wsSessionIdsWithEndpoint.forEach((dest, wsSessions) -> {
                                        Response notification = new GameHasBeenUpdatedNotification(game.getName());
                                        convertAndSendToUsersInternal(wsSessions, Destination.Utils.convertSubscribeDestToSendDest(dest), notification);
                                        log.info("Notification was sent to destination {}, for player {}, for simpSessionIds {}",
                                                Destination.Utils.convertSubscribeDestToSendDest(dest), playerInstance.getId(), wsSessionIds);
                                    });
                                } else {
                                    wsSessionIds.forEach(simpSessionId -> {
                                        Response response = createResponseWithGameForPlayer(game, playerInstance);
                                        template.convertAndSendToUser(simpSessionId, sendDestinationEndpoint, response);
                                        log.info("Response was sent to destination {}, for player {}, simpSessionId {}",
                                                sendDestinationEndpoint, playerInstance.getId(), simpSessionId);
                                    });
                                }
                            }
                    )
            );
        });
        log.info("Sending notification for all players for game is complete", game.getId());
    }

    private Response createResponseWithGameForPlayer(GameInstance game, PlayerInstance player) {
        Response response;
        try {
            response = creatorService.retryGetGameResponseForPlayer(game, player);
        } catch (GameError error) {
            String message = error.getMessage();
            log.error(message, error);
            response = new NotificationResponse(NotificationType.ERROR, message);
        }
        return response;
    }

    public void notifyOwnerThatGameHasStartedAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyOwnerThatGameHasStarted(game));
    }

    public void notifyOwnerThatGameHasStarted(GameInstance game) {
        PlayerInstance owner = game.getOwner();
        Response ownerNotification = new GameHasStartedForOwnerNotification(game.getName());
        ImmutableList<UUID> playerIds = ImmutableList.of(owner.getId());
        notifyUsersInternal(ownerNotification, playerIds);
        log.info("Notification that game has started was sent for user {}", owner.getId());
    }

    public void notifyBeforeDeleteRunningGameAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyBeforeDeleteRunningGame(game));
    }

    public void notifyBeforeDeleteRunningGame(GameInstance game) {
        Response notification = new BeforeDeleteGameNotification(game.getName());
        partyRepository.findByGame(game).ifPresent(
                party -> {
                    Optional<GameActionRecord> record = recordRepository.findFirstByPartyOrderByDateUpdatedDesc(party);
                    Set<UUID> userIds = getUserIdsNeededNotify(game, party, record);
                    if (CollectionUtils.isNotEmpty(userIds)) {
                        notifyUsersInternal(notification, userIds);
                        log.info("Notification that before deleting running game was sent for players {}", userIds);
                    }
                }
        );
    }

    public void notifyDeleteGameAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyDeleteGame(game));
    }

    public void notifyDeleteGame(GameInstance game) {
        Response notification = new DeleteGameNotification(game.getName());
        notifyGameDestInternal(game, notification);
    }

    private void notifyGameDestInternal(GameInstance game, Response notification) {
        Set<UUID> userIds = new HashSet<UUID>() {{
            add(game.getOwner().getId());
        }};
        partyRepository.findByGame(game).ifPresent(party -> userIds.add(party.getPlayer().getId()));
        if (CollectionUtils.isNotEmpty(userIds)) {
            notifyUsersInternal(notification, userIds);
            log.info("Notification that game deleted was sent for players {}", userIds);
        }
    }

    public void notifyCompleteGameAsync(GameInstance game) {
        asyncExecutor.execute(() -> notifyCompleteGame(game));
    }

    public void notifyCompleteGame(GameInstance game) {
        Response notification = new GameOverNotification(game.getName());
        notifyGameDestInternal(game, notification);
    }

    private Set<UUID> getUserIdsNeededNotify(GameInstance game, GameParty party, Optional<GameActionRecord> record) {
        Set<UUID> userIds = new HashSet<>();
        if (record.isPresent()) {
            if (record.get().isApproved()) {
                userIds.add(party.getPlayer().getId());
            } else {
                userIds.add(game.getOwner().getId());
            }
        } else {
            userIds.add(party.getPlayer().getId());
        }
        return userIds;
    }

    private void notifyUsersInternal(Response notification, Collection<UUID> userIds) {
        userIds.forEach(userId -> contractRepository.getAllByPlayerId(userId).forEach(contractProjection -> {
            String jSessionId = contractProjection.getJSessionId();
            Map<String, Collection<String>> wsSessionIdsWithEndpoint = webSocketSessionAssistant.getWsSessionIdsWithEndpoint(jSessionId);
            wsSessionIdsWithEndpoint.forEach((dest, wsSessions) ->
                    convertAndSendToUsersInternal(wsSessions, Destination.Utils.convertSubscribeDestToSendDest(dest), notification));
        }));
    }

    private void convertAndSendToUsersInternal(Collection<String> users, String destination, Response payload) throws MessagingException {
        if (CollectionUtils.isNotEmpty(users)) {
            users.forEach(user -> template.convertAndSendToUser(user, destination, payload));
        }
    }

    public void convertAndSentWithCallback(NotifyCallback callback) {
        callback.getNotifySets().forEach(notifySet -> template.convertAndSendToUser(notifySet.getSimpSessionId(),
                notifySet.getDestination(),
                notifySet.getResponseProvider().get()));
    }
}
