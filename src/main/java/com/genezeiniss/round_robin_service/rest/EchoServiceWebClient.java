package com.genezeiniss.round_robin_service.rest;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import com.genezeiniss.round_robin_service.exception.EchoServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EchoServiceWebClient {

    private final WebClient webClient;
    private final EchoServiceProperties properties;

    @Value("${timeout.endpoint}")
    private Duration endpointTimeout;
    @Value("${timeout.health-check}")
    private Duration healthCheckTimeout;

    public Map<String, Object> echo(String url, Map<String, Object> request) {
        try {
            return webClient.post()
                    .uri(url + properties.endpoint())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block(endpointTimeout);
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof TimeoutException) {
                throw new EchoServiceException(url, String.format("timeout after %s seconds", endpointTimeout.getSeconds()));
            }
            throw new EchoServiceException(url, String.format("request failed: %s", exception.getMessage()));
        }
    }

    public boolean healthCheck(String url) {
        log.debug("Checking health for {} on thread {}", url, Thread.currentThread());

        try {
            ClientResponse response = webClient.get()
                    .uri(url + properties.healthEndpoint())
                    .exchangeToMono(Mono::just)
                    .block(healthCheckTimeout);

            if (HttpStatus.OK.equals(Objects.requireNonNull(response).statusCode())) {
                return true;
            }
        } catch (Exception exception) {
            log.error("Health check for {} failed :: {}", url, exception.getMessage());
        }
        return false;
    }
}
