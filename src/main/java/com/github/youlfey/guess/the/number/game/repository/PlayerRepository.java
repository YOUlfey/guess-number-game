package com.github.youlfey.guess.the.number.game.repository;

import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.iterable.LazyPageIterator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

import static com.github.youlfey.guess.the.number.game.configuration.cache.CachingConfiguration.PLAYER_CACHE;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerInstance, UUID> {

    @Override
    @CachePut(value = PLAYER_CACHE, key = "#entity.id")
    <S extends PlayerInstance> S save(S entity);

    @Cacheable(value = PLAYER_CACHE, key = "#id")
    default PlayerInstance findByIdOrNull(UUID id) {
        return findById(id).orElse(null);
    }

    @Override
    @CacheEvict(value = PLAYER_CACHE, key = "#entity.id")
    void delete(PlayerInstance entity);

    @CacheEvict(value = PLAYER_CACHE)
    void deleteAllByIdIsIn(Collection<UUID> ids);

    default Iterable<PlayerInstance> iterableAllPlayers() {
        return () -> new LazyPageIterator<>(this::findAll);
    }
}
