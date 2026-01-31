package com.example.usercenterpractice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.usercenterpractice.mapper")
public class UserCenterpracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterpracticeApplication.class, args);
    }

}
