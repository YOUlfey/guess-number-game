package com.github.youlfey.guess.the.number.game.service.assistant;

import com.github.youlfey.guess.the.number.game.configuration.GameConfiguration;
import com.github.youlfey.guess.the.number.game.domain.ContractPlayerIdWithJSessionId;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.pojo.SessionInformation;
import com.github.youlfey.guess.the.number.game.repository.ContractRepository;
import com.github.youlfey.guess.the.number.game.service.enrich.SessionEnrichment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentHttpSessionAssistant {
    private final ContractRepository contractRepository;
    private final SessionInformation sessionInformation;
    @Qualifier(GameConfiguration.OPTIMISTIC_RETRY)
    private final RetryTemplate retryTemplate;

    /**
     * Get current contract from current session
     * Contract set in SessionEnrichment
     * @see SessionEnrichment#enrichSessionWithCurrentContract(javax.servlet.http.HttpSession, org.springframework.http.HttpHeaders)
     * @return current player
     */
    public ContractPlayerIdWithJSessionId getCurrentContract() {
        String jSessionId = getJSessionId();
        log.info("Current http session {}", jSessionId);
        return retryTemplate.execute(ctx -> contractRepository.mustFindById(jSessionId));
    }

    public PlayerInstance getCurrentPlayer() {
        return getCurrentContract().getPlayer();
    }

    private String getJSessionId() {
        return sessionInformation.getJSessionId();
    }
}
