package com.genezeiniss.round_robin_service.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EchoServiceWebClient {

    private final WebClient webClient;
    @Value("${echo-service.endpoint}")
    private String echoEndpoint;
    @Value("${echo-service.health-endpoint}")
    private String healthEndpoint;

    public Map<String, Object> echo(String url, Map<String, Object> request) {
        return webClient.post()
                .uri(url + echoEndpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block(); // todo: remove when moving to async / react handling
    }

    public boolean health(String url) {
        try {
            webClient.get()
                    .uri(url + healthEndpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                    .block();
        } catch (Exception exception) {
            log.error("Health check of instance {} failed :: {}", url, exception.getMessage());
            return false;
        }
        return true;
    }
}
