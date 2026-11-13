package ru.yandex.practicum.mymarket;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfiguration {

    private static RedisServer redisServer;

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws IOException {
        if (redisServer == null || !redisServer.isActive()) {
            redisServer = new RedisServer(6370);
            redisServer.start();
        }
        return redisServer;
    }
}
