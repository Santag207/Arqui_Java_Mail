package com.tours.paymentservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("accountTransactionManager") PlatformTransactionManager accountTransactionManager) {
        return accountTransactionManager;
    }
}