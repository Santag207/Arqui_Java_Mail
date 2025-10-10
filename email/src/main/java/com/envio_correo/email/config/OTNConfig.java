package com.envio_correo.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableRetry
@PropertySource("classpath:otn.properties")
public class OTNConfig {

    @Value("${otn.service.url}")
    private String otnServiceUrl;

    @Value("${otn.service.timeout:5000}")
    private int timeout;

    @Bean
    public WebClient otnWebClient() {
        return WebClient.builder()
                .baseUrl(otnServiceUrl)
                .build();
    }
}