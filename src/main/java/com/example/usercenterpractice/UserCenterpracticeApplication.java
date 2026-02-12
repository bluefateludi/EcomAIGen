package com.example.usercenterpractice;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.example.usercenterpractice.mapper")
public class UserCenterpracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterpracticeApplication.class, args);
    }

}
