package com.github.youlfey.guess.the.number.game;

import com.github.youlfey.guess.the.number.game.domain.GameState;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.service.cleaner.GameCleaner;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class GuessTheNumberGameStarter {
    public static void main(String[] args) {
        SpringApplication.run(GuessTheNumberGameStarter.class, args);
    }

    private final GameRepository gameRepository;
    private final GameCleaner cleanerService;
    @PostConstruct
    public void postInit() {
        gameRepository.findAllByStateIsIn(ImmutableList.of(GameState.RUNNING, GameState.CREATED))
                .forEach(game -> cleanerService.resolveExpiredForGame(game.getGameId()));
    }
}
