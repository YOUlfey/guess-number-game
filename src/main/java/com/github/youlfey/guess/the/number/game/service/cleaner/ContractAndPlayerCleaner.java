package com.github.youlfey.guess.the.number.game.service.cleaner;

import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.repository.GamePartyRepository;
import com.github.youlfey.guess.the.number.game.repository.GameRepository;
import com.github.youlfey.guess.the.number.game.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class ContractAndPlayerCleaner {
    private final ContractRepository contractRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final GamePartyRepository partyRepository;

    @Scheduled(fixedDelay = 86400000L)
    public void removeContractIfUpdatedDateMoreThanOneMonth() {
        LocalDateTime nowMinusOneDay = LocalDateTime.now().minusMonths(1);
        // remove contracts expired by one month
        contractRepository.deleteAllByUpdatedDateBefore(nowMinusOneDay);
    }

    @Scheduled(fixedDelay = 86400000L)
    public void removePlayerIfNotExistsRelations() {
        Collection<UUID> needRemovePlayerIds = new HashSet<>();
        playerRepository.iterableAllPlayers().forEach(player -> {
            if (!contractRepository.existsByPlayer(player) && !gameRepository.existsByOwner(player) && !partyRepository.existsByPlayer(player)) {
                needRemovePlayerIds.add(player.getId());
            }
        });
        needRemovePlayerIds.forEach(playerRepository::deleteById);
    }

}
