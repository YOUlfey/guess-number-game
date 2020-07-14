package com.github.youlfey.guess.the.number.game.repository;

import com.github.youlfey.guess.the.number.game.domain.GameActionRecord;
import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import com.github.youlfey.guess.the.number.game.service.cleaner.GameCleaner;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface ActionRecordRepository extends JpaRepository<GameActionRecord, UUID> {
    Optional<GameActionRecord> findFirstByPartyOrderByDateUpdatedDesc(GameParty party);

    long countGameActionRecordByApprovedTrue();

    @Transactional
    void deleteAllByApprovedTrue();

    /**
     * Methods deleteAllByParty_Game and deleteAllByParty_GameAndParty_Game_UpdatedDateBefore for internal purposes
     * @see GameCleaner#resolveExpiredForGame(UUID)
     */
    Optional<GameActionRecord> findFirstByParty_GameOrderByDateUpdatedDesc(GameInstance game);
    @Transactional
    void deleteAllByParty_Game(GameInstance game);
    @Transactional
    void deleteAllByParty_Game_Id(UUID gameId);
}
