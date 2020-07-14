package com.github.youlfey.guess.the.number.game.service.cleaner;

import com.github.youlfey.guess.the.number.game.configuration.GameConfiguration;
import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameState;
import com.github.youlfey.guess.the.number.game.pojo.callback.NotifyCallback;
import com.github.youlfey.guess.the.number.game.repository.ActionRecordRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.service.SubscriberNotificationService;
import com.github.youlfey.guess.the.number.game.task.GameExpiredTask;
import com.github.youlfey.guess.the.number.game.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class GameCleaner {
    private static final int PAST_MINUTES_FOR_AVAILABLE_TO_REMOVE_ENDED_GAME = 10;
    private static final long ADDITIONALLY_MILLIS_EXPIRED = 60000L;
    private final GameRepository gameRepository;
    private final ActionRecordRepository recordRepository;
    private final GamePartyRepository partyRepository;
    @Qualifier(GameConfiguration.THREAD_POOL_TASK_SCHEDULER_FOR_EXPIRE_GAMES)
    private final ThreadPoolTaskScheduler scheduler;
    private final SubscriberNotificationService notificationService;

    public void resolveExpiredForGame(UUID gameId) {
        log.info("Resolve expiration for game {}", gameId);
        Optional<GameInstance> gameOptional = gameRepository.findById(gameId);
        gameOptional.ifPresent(game -> {
            GameState currentState = game.getState();
            if (GameState.ENDED.equals(currentState)) {
                return;
            }
            if (GameState.CREATED.equals(currentState)) {
                if (DateUtils.getMilliSecondsWithNow(game.getUpdatedDate()) >= DateUtils.minutesToMilliSeconds(game.getExpired())) {
                    log.info("Remove game {} with state {}", gameId, game.getState());
                    gameRepository.delete(game);
                    notificationService.notifyAllSubscribersForGamesEndpointAsync();
                } else {
                    scheduleGame(game.getId(), game.getUpdatedDate(), DateUtils.minutesToMilliSeconds(game.getExpired()));
                }
                return;
            }

            Optional<GameActionRecord> lastRecordOptional = recordRepository.findFirstByParty_GameOrderByDateUpdatedDesc(game);

            if (lastRecordOptional.isPresent()) {
                GameActionRecord lastRecord = lastRecordOptional.get();
                LocalDateTime dateUpdated = lastRecord.getDateUpdated();
                removeGameRelationsIfPossible(game, dateUpdated);
            } else {
                LocalDateTime updatedDate = game.getUpdatedDate();
                removeGameRelationsIfPossible(game, updatedDate);
            }
        });
    }

    private void removeGameRelationsIfPossible(GameInstance game, LocalDateTime dateUpdated) {
        UUID gameId = game.getId();
        Long totalMilliSeconds = DateUtils.getMilliSecondsWithNow(dateUpdated);
        Long expiredMilliSeconds = DateUtils.minutesToMilliSeconds(game.getExpired());
        log.info("Currently total milliseconds from updated date is {}, and expired milliseconds is {}", totalMilliSeconds, expiredMilliSeconds);
        if (totalMilliSeconds < expiredMilliSeconds) {
            scheduleGame(game.getId(), dateUpdated, DateUtils.minutesToMilliSeconds(game.getExpired()));
        } else if (compareMillis(totalMilliSeconds, expiredMilliSeconds)) {
            notificationService.notifyBeforeDeleteRunningGameAsync(game);
            scheduleGame(game.getId(), dateUpdated, DateUtils.minutesToMilliSeconds(game.getExpired()) + ADDITIONALLY_MILLIS_EXPIRED);
        } else {
            notificationService.notifyDeleteGameAsync(game);
            NotifyCallback callback = notificationService.createCallBackForNotifyAllSubscribersForGamesEndpointByCurrentGame(game);
            log.info("Remove game {} with all record, because expired milliseconds {} more than total milliseconds {}", gameId, expiredMilliSeconds, totalMilliSeconds);
            removeGameRelations(game);
            notificationService.convertAndSentWithCallback(callback);
        }
    }

    private void scheduleGame(UUID gameId, LocalDateTime updatedDate, long millis) {
        //скедулим еще раз если с даты обновления хистори рекордов не прошло expired минут
        LocalDateTime ldtWithExpiredMinutes = DateUtils.localDateTimePlusMillis(updatedDate, millis);
        scheduleGameWithDate(gameId, DateUtils.convertFromLocalDateTime(ldtWithExpiredMinutes));
    }


    private void removeGameRelations(GameInstance game) {
        recordRepository.deleteAllByParty_Game(game);
        partyRepository.deleteByGame(game);
        gameRepository.delete(game);

    }

    @Scheduled(fixedDelay = 1800000L)
    public void removeEndedGameAndEvictRecords() {
        LocalDateTime dateAvailableForDelete = LocalDateTime.now().minusMinutes(PAST_MINUTES_FOR_AVAILABLE_TO_REMOVE_ENDED_GAME);
        gameRepository.findAllByStateAndUpdatedDateBefore(GameState.ENDED, dateAvailableForDelete)
                .forEach(gameIdProjection -> {
                            UUID gameId = gameIdProjection.getGameId();
                            deletePartyAndRecordsInternal(gameId);
                        }
                );
    }

    @Transactional
    protected void deletePartyAndRecordsInternal(UUID gameId) {
        recordRepository.deleteAllByParty_Game_Id(gameId);
        partyRepository.deleteAllByGame_Id(gameId);
        gameRepository.deleteById(gameId);
    }

    public void scheduleGame(GameInstance game) {
        Date dateScheduling = new Date(System.currentTimeMillis() + game.getExpired() * 60000);
        scheduleGameWithDate(game.getId(), dateScheduling);
    }

    private void scheduleGameWithDate(UUID gameId, Date dateScheduling) {
        log.info("Scheduling game {} with date {}", gameId, dateScheduling);
        scheduler.schedule(new GameExpiredTask(gameId, this), dateScheduling);
    }

    private static final long thresh = 3000;

    private static boolean compareMillis(long l1, long l2) {
        if (l1 == l2) return true;
        long max = Long.max(l1, l2);
        long leftBound = max - thresh;
        long rightBound = max + thresh;
        return l1 > leftBound && l1 < rightBound && l2 > leftBound && l2 < rightBound;
    }
}
