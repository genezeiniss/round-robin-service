package com.genezeiniss.round_robin_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RoundRobinConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
