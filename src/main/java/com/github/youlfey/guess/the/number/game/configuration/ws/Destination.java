package com.github.youlfey.guess.the.number.game.configuration.ws;

import com.github.youlfey.guess.the.number.game.util.UUIDUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode
public class Destination {
    @Getter
    private final String endpoint;
    @Getter
    private final TypeDestination type;

    private static Destination SUBSCRIBE_GAMES_ENDPOINT = new Destination(TypeDestination.SUBSCRIBE, "/user/queue/games");
    private static Destination SEND_GAMES_ENDPOINT = new Destination(TypeDestination.SEND, "/queue/games");


    private Destination(TypeDestination type, String endpoint, Object... params) {
        this.type = type;
        this.endpoint = String.format(endpoint, params);
    }

    public static Destination getSubscribeGamesEndpoint() {
        return SUBSCRIBE_GAMES_ENDPOINT;
    }

    public static Destination getSendGamesEndpoint() {
        return SEND_GAMES_ENDPOINT;
    }

    public static Destination getSubscribeGameEndpoint(UUID gameId) {
        return new Destination(TypeDestination.SUBSCRIBE, "/user/queue/game?id=%s", gameId);
    }

    public static Destination getSendGameEndpoint(UUID gameId) {
        return new Destination(TypeDestination.SEND, "/queue/game?id=%s", gameId);
    }

    public enum TypeDestination {
        SUBSCRIBE, SEND
    }

    public static class Utils {
        private static final Predicate<String> patternEndedGame = Pattern.compile("/user/queue/game\\?id=[a-zA-Z0-9\\-]+$").asPredicate();
        private static final Predicate<String> patternEndedGames = Pattern.compile("/user/queue/games$").asPredicate();
        private static final Pattern getGameIdPattern = Pattern.compile("[a-zA-Z0-9\\-]+$");

        public static boolean isSpecifyGameDestination(String endpoint) {
            return patternEndedGame.test(endpoint);
        }

        public static boolean isGamesDestination(String endpoint) {
            return patternEndedGames.test(endpoint);
        }

        public static String convertSubscribeDestToSendDest(String endpoint) {
            return StringUtils.startsWith(endpoint, "/user") ? endpoint.substring(5) : endpoint;
        }

        public static Optional<UUID> getGameIdFromEndpoint(String endpoint) {
            Optional<String> strId = getIdAsStrFromEndpoint(endpoint);
            if (strId.isPresent()) {
                if (UUIDUtils.isValidUUID(strId.get())) {
                    return Optional.of(UUID.fromString(strId.get()));
                }
                return Optional.empty();
            } else {
                return Optional.empty();
            }
        }

        public static Optional<String> getIdAsStrFromEndpoint(String endpoint) {
            Matcher matcher = getGameIdPattern.matcher(endpoint);
            if (matcher.find()) {
                return Optional.of(matcher.group());
            }
            return Optional.empty();
        }
    }
}
