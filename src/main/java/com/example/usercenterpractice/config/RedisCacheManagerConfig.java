package com.example.usercenterpractice.config;

import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheManagerConfig {

        @Resource
        private RedisConnectionFactory redisConnectionFactory;

        @Bean
        public CacheManager cacheManager() {

                // 默认配置
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)) // 默认 30 分钟过期
                                .disableCachingNullValues() // 禁用 null 值缓存
                                // key 使用 String 序列化器
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                // value 使用 JDK 序列化器（保持类型信息）
                                // 注意：参考教程项目，不使用JSON序列化以避免类型丢失问题
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new org.springframework.data.redis.serializer.JdkSerializationRedisSerializer()));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultConfig)
                                // 针对 good_app_page 配置5分钟过期
                                .withCacheConfiguration("good_app_page",
                                                defaultConfig.entryTtl(Duration.ofMinutes(5)))
                                .build();
        }
}
