package com.historical.voting.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync // 启用异步
@EnableKafka // 启用Kafka
@EnableScheduling // 启用定时任务
@SpringBootApplication
@EnableTransactionManagement // 启用事务管理
@EntityScan(basePackages = "com.historical.voting.*.entity")
@EnableJpaRepositories(basePackages = "com.historical.voting.*.repository")
@MapperScan("com.historical.voting.user.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 