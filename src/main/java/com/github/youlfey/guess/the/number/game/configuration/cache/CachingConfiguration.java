package com.github.youlfey.guess.the.number.game.configuration.cache;

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CachingConfiguration {

    private static final long DURATION_EXPIRATION_IN_HOURS = 1L;
    private static final long MAXIMUM_CACHE_SIZE = 200;
    private static final String CACHE_MANAGER = "ptCacheManager";
    public static final String PLAYER_CACHE = "player_cache";
    public static final String GAME_CACHE = "game_cache";

    @Bean(CACHE_MANAGER)
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterAccess(DURATION_EXPIRATION_IN_HOURS, TimeUnit.HOURS)
                                .maximumSize(MAXIMUM_CACHE_SIZE)
                                .build()
                                .asMap(),
                        false);
            }
        };
    }
}
