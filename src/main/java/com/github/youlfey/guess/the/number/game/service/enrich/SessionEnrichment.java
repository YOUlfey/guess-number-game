package com.github.youlfey.guess.the.number.game.service.enrich;

import com.github.youlfey.guess.the.number.game.configuration.GameConfiguration;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.pojo.SessionInformation;
import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.repository.PlayerRepository;
import com.github.youlfey.guess.the.number.game.util.UUIDUtils;
import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionEnrichment {

    private final PlayerRepository playerRepository;
    private final ContractRepository contractRepository;
    private final SessionInformation sessionInformation;
    @Qualifier(GameConfiguration.OPTIMISTIC_RETRY)
    private final RetryTemplate retryTemplate;
    public static final String HEADER_USER_NAME = "player-id";

    public void enrichSessionWithCurrentContract(HttpSession session, HttpHeaders httpHeaders) {
        final List<String> playerIds = httpHeaders.get(HEADER_USER_NAME);
        String playerId = CollectionUtils.isEmpty(playerIds) ? null : playerIds.get(0);
        final String jSessionId = session.getId();
        resolveContract(playerId, jSessionId);
        sessionInformation.setJSessionId(jSessionId);
    }

    /**
     * Logic of this method presented below:
     * if(header player-simpSessionId with predefined current player simpSessionId is not null and this player exists in database)
     *      search contract with JSESSIONID
     *      if(contract is not null)
     *          set current player to contract and save contract
     *      if(contract is null)
     *          create contract with current player and save
     *      save current player in session attributes and return immediately
     * if(player does not exist in database)
     *      search contract with JSESSIONID
     *      if(contract is not null)
     *          get current player from contract, save current player in session attributes and return immediately
     *      if(contract is null)
     *          create new player and save, create new contract and save, save current player in session attributes
     */
    private void resolveContract(String playerId, String jSessionId) {
        if (StringUtils.isEmpty(jSessionId)) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "JSESSIONID should be presented");
        }
        log.info("Current JSESSIONID {}", jSessionId);
        if (StringUtils.isNotEmpty(playerId) && UUIDUtils.isValidUUID(playerId)) {
            PlayerInstance currentPlayer = playerRepository.findByIdOrNull(UUID.fromString(playerId));
            if (Objects.nonNull(currentPlayer)) {
                log.info("Found player by simpSessionId {}", playerId);
                createContractOrEnrichWithCurrentPlayer(jSessionId, currentPlayer);
                return;
            }
        }

        if (contractRepository.notExistsById(jSessionId)) {
            PlayerInstance draftCurrentPlayer = PlayerInstance.builder().build();
            PlayerInstance createdCurrentPlayer = playerRepository.save(draftCurrentPlayer);
            ContractPlayerIdWithJSessionId createdContract = createAndSaveContract(jSessionId, createdCurrentPlayer);
            log.info("New player created by JSESSIONID {}, playerId {}", jSessionId, createdCurrentPlayer.getId());
            log.debug("Current player is {}, contract is {}", createdCurrentPlayer, createdContract);
        }
    }

    private void createContractOrEnrichWithCurrentPlayer(String jSessionId, PlayerInstance currentPlayer) {
        retryTemplate.execute(ctx -> {
            Optional<ContractPlayerIdWithJSessionId> optionalContract = contractRepository.findById(jSessionId);
            if (optionalContract.isPresent()) {
                ContractPlayerIdWithJSessionId contract = optionalContract.get();
                if (!currentPlayer.equals(contract.getPlayer())) {
                    contract.setPlayer(currentPlayer);
                    contractRepository.save(contract);
                }
            } else {
                createAndSaveContract(jSessionId, currentPlayer);
            }
            return null;
        });

        ContractPlayerIdWithJSessionId currentContract = contractRepository.mustFindById(jSessionId);
        log.debug("Current player is {}, contract {}", currentPlayer, currentContract);
    }

    private ContractPlayerIdWithJSessionId createAndSaveContract(String jSessionId, PlayerInstance currentPlayer) {
        ContractPlayerIdWithJSessionId draftContract = ContractPlayerIdWithJSessionId
                .builder()
                .id(jSessionId)
                .player(currentPlayer)
                .build();
        return contractRepository.save(draftContract);
    }
}
