package com.tvpsystem.compras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ComprasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComprasServiceApplication.class, args);
    }
}