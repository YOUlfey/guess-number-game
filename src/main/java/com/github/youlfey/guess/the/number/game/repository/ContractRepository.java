package com.github.youlfey.guess.the.number.game.repository;

import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import com.github.youlfey.guess.the.number.game.domain.projection.ContractProjection;
import com.github.youlfey.guess.the.number.game.iterable.LazyPageIterator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<ContractPlayerIdWithJSessionId, String> {
    // for internal purpose, contracts with expired by one month need to clean
    void deleteAllByUpdatedDateBefore(LocalDateTime dateTime);

    Page<ContractProjection> findAllByPlayerId(Pageable pageable, UUID playerId);

    default ContractPlayerIdWithJSessionId mustFindById(String jSessionId) {
        ContractPlayerIdWithJSessionId contract = findById(jSessionId).orElseThrow(() -> new OptimisticLockingFailureException("Contract should be presented"));
        contract.setUpdatedDate(LocalDateTime.now());
        return save(contract);
    }

    default boolean notExistsById(String jSessionId) {
        return !existsById(jSessionId);
    }

    boolean existsByPlayer(PlayerInstance player);

    default Iterable<ContractProjection> getAllByPlayerId(UUID playerId) {
        return () -> new LazyPageIterator<>(pageable -> findAllByPlayerId(pageable, playerId));
    }
}
