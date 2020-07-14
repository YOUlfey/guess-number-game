package com.github.youlfey.guess.the.number.game.repository;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.GameState;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.domain.projection.GameIdProjection;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.configuration.cache.CachingConfiguration.GAME_CACHE;
import static com.github.youlfey.guess.the.number.game.exception.ErrorFactory.gameNotFound;

@Repository
public interface GameRepository extends JpaRepository<GameInstance, UUID> {
    Page<GameInstance> findAllByOwnerNotAndStateIsIn(Pageable pageable, PlayerInstance owner, Collection<GameState> states);
    Page<GameInstance> findAllByOwnerAndStateIsIn(Pageable pageable, PlayerInstance owner, Collection<GameState> states);

    @Override
    @CacheEvict(cacheNames = GAME_CACHE)
    void deleteById(UUID gameId);

    /**
     * Only for internal purpose, not public api
     */
    Collection<GameIdProjection> findAllByStateIsIn(Collection<GameState> states);

    Collection<GameIdProjection> findAllByStateAndUpdatedDateBefore(GameState state, LocalDateTime updatedDate);

    boolean existsByOwner(PlayerInstance player);

    @Override
    @CachePut(value = GAME_CACHE, key = "#entity.id")
    <S extends GameInstance> S save(S entity);

    @Cacheable(value = GAME_CACHE, key = "#gameId")
    default GameInstance mustFindById(UUID gameId) {
        return findById(gameId).orElseThrow(() -> gameNotFound(gameId));
    }
}
