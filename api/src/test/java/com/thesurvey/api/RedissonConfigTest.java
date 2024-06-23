package com.thesurvey.api;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedissonConfigTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedissonClient() {
        // Ensure the RedissonClient is not null
        assertThat(redissonClient).isNotNull();

        RBucket<String> bucket = redissonClient.getBucket("testKey");
        bucket.set("testValue");

        String value = bucket.get();
        assertThat(value).isEqualTo("testValue");

        // Clean up
        bucket.delete();
    }
}
