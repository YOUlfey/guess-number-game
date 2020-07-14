package com.github.youlfey.guess.the.number.game.service.assistant;

import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


@Component
public class WebSocketSessionAssistant {

    private final Map<String, Map<String, String>> cacheMappingEndpointToCacheMappingJSessionIdToWebSocket;

    public WebSocketSessionAssistant() {
        this.cacheMappingEndpointToCacheMappingJSessionIdToWebSocket = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .<String, Map<String, String>>build().asMap();
    }

    /**
     * Create mapping jSession to web socket
     * used in getCacheByDestinationOrCreate
     *
     * @see WebSocketSessionAssistant#getCacheByDestinationOrCreate(java.lang.String)
     */
    private Map<String, String> newInstanceWSMapping() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .<String, String>build().asMap();
    }

    /**
     * Save websocket session simpSessionId by jSession simpSessionId and by destination
     */
    public void persist(String destination, String jSessionId, String simpSessionId) {
        Map<String, String> cache = getCacheByDestinationOrCreate(destination);
        cache.put(jSessionId, simpSessionId);
    }

    /**
     * Internal method
     * The method get cache with mapping jsession simpSessionId to websocket by destination
     * used in persist
     *
     * @see WebSocketSessionAssistant#persist(java.lang.String, java.lang.String, java.lang.String)
     */
    private synchronized Map<String, String> getCacheByDestinationOrCreate(String destination) {
        Map<String, String> cache = cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.get(destination);
        if (Objects.isNull(cache)) {
            cache = newInstanceWSMapping();
            cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.put(destination, cache);
        }
        return cache;
    }

    public Map<String, String> getMappingJSessionToWsSessionByDestination(String destination) {
        return cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.get(destination);
    }

    /**
     * Get all simpSessionIds for destination and JSESSIONID-s
     *
     * @param destination current destination
     * @param jSessionIds filtered by following JSESSIONID-s
     */
    public Collection<String> getWsSessionIds(String destination, String... jSessionIds) {
        Map<String, String> mapping = cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.get(destination);
        if (Objects.isNull(jSessionIds) || jSessionIds.length == 0 || MapUtils.isEmpty(mapping)) {
            return Collections.emptyList();
        }
        Collection<String> filter = Arrays.asList(jSessionIds);
        return getWsSessionIdsInternal(mapping, filter);
    }

    private Collection<String> getWsSessionIdsInternal(Map<String, String> mapping, Collection<String> filter) {
        if (MapUtils.isEmpty(mapping)) {
            return Collections.emptyList();
        }
        return mapping
                .entrySet().stream()
                .filter(jToWs -> filter.contains(jToWs.getKey()))
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    /**
     * Mapping destination to simpSessionIds
     * Example:
     * {
     * "destination1": ["simpSessionId1", "simpSessionId2"],
     * "destination2": ["simpSessionId3"]
     * }
     *
     * @param jSessionIds filtered by following JSESSIONID-s
     */
    public Map<String, Collection<String>> getWsSessionIdsWithEndpoint(String... jSessionIds) {
        return cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), getWsSessionIds(entry.getKey(), jSessionIds)))
                .filter(pair -> CollectionUtils.isNotEmpty(pair.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Deprecated
    public Map<String, String> getMappingJSessionToWsSessionByDestinationAndJSessionIds(String destination, Collection<String> jSessionIds) {
        Map<String, String> mappingJSessionToWs = getMappingJSessionToWsSessionByDestination(destination);
        boolean notEmpty = CollectionUtils.isNotEmpty(jSessionIds);
        return mappingJSessionToWs.entrySet().stream().filter(entry -> notEmpty && jSessionIds.contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Remove websocket session simpSessionId if presented
     *
     * @param simpSessionId - websocket session simpSessionId
     */
    public void removeBySimpSessionId(String simpSessionId) {
        if (Objects.isNull(simpSessionId)) return;
        cacheMappingEndpointToCacheMappingJSessionIdToWebSocket.values()
                .forEach(jSessionToWsMap -> jSessionToWsMap.values().removeIf(simpSessionId::equals));
    }
}
