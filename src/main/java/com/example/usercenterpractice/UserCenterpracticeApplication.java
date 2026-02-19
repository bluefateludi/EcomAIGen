package com.example.usercenterpractice;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = { RedisEmbeddingStoreAutoConfiguration.class })
@MapperScan("com.example.usercenterpractice.mapper")
@EnableCaching
public class UserCenterpracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterpracticeApplication.class, args);
    }

}
