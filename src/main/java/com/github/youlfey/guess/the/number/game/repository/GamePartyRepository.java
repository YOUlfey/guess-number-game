package com.github.youlfey.guess.the.number.game.repository;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameState;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.domain.GameParty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.exception.ErrorFactory.partyNotFound;

@Repository
public interface GamePartyRepository extends JpaRepository<GameParty, UUID> {
    Optional<GameParty> findByGame(GameInstance game);
    Optional<GameParty> findByGame_Id(UUID gameId);
    Page<GameParty> findAllByPlayerAndGame_StateIsIn(Pageable pageable, PlayerInstance player, Collection<GameState> states);
    @Transactional
    void deleteByGame(GameInstance game);
    @Transactional
    void deleteAllByGame_Id(UUID gameId);
    boolean existsByPlayer(PlayerInstance player);

    default GameParty mustFindByGame(GameInstance game) {
        return findByGame(game).orElseThrow(() -> partyNotFound(game.getId()));
    }

    default GameParty mustFindByGameId(UUID gameId) {
        return findByGame_Id(gameId).orElseThrow(() -> partyNotFound(gameId));
    }
}
