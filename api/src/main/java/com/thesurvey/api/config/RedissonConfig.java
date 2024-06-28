package com.thesurvey.api.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
//        config.useSentinelServers()
//                .setMasterName("mymaster")
//                .addSentinelAddress("redis://127.0.0.1:26379", "redis://127.0.0.1:26380", "redis://127.0.0.1:26381");

        return Redisson.create(config);
    }
}
