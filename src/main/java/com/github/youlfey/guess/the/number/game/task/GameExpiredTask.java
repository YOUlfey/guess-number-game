package com.github.youlfey.guess.the.number.game.task;

import com.github.youlfey.guess.the.number.game.service.cleaner.GameCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Scope(value = "prototype")
public class GameExpiredTask implements Runnable {
    private final UUID entityId;
    private final GameCleaner cleanerService;

    @Override
    public void run() {
        cleanerService.resolveExpiredForGame(entityId);
    }
}
