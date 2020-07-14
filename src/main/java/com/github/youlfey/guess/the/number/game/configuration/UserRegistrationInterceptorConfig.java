package com.github.youlfey.guess.the.number.game.configuration;

import com.github.youlfey.guess.the.number.game.util.HttpUtils;
import com.github.youlfey.guess.the.number.game.service.enrich.SessionEnrichment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.github.youlfey.guess.the.number.game.configuration.GameConfiguration.SECURE_PASSWORD_FOR_INTERNAL_API;

@Configuration
@RequiredArgsConstructor
public class UserRegistrationInterceptorConfig implements WebMvcConfigurer {

    private final SessionEnrichment sessionEnrichment;
    @Qualifier(SECURE_PASSWORD_FOR_INTERNAL_API)
    private final String securePasswordForInternalApi;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        final HttpSession session = request.getSession();
                        HttpHeaders httpHeaders = HttpUtils.getHttpHeadersFromHttpServletRequest(request);
                        sessionEnrichment.enrichSessionWithCurrentContract(session, httpHeaders);
                        return true;
                    }
                }
        ).addPathPatterns("/api/game/**", "/api/polling/**");
        registry.addInterceptor(
                new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        HttpHeaders httpHeaders = HttpUtils.getHttpHeadersFromHttpServletRequest(request);
                        List<String> passwords = httpHeaders.get(SECURE_PASSWORD_FOR_INTERNAL_API);
                        if (CollectionUtils.isEmpty(passwords) || !passwords.get(0).equals(securePasswordForInternalApi)) {
                            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                        }
                        return true;
                    }
                }
        ).addPathPatterns("/api/internal/**");
    }
}
