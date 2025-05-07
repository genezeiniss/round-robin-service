package com.genezeiniss.round_robin_service.rest;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import com.genezeiniss.round_robin_service.exception.EchoServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class EchoServiceWebClientTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String ENDPOINT = "/api/echo";
    private static final String HEALTH_ENDPOINT = "/health";
    private static final Map<String, Object> TEST_REQUEST = Map.of("key", "value");
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private ClientResponse clientResponse;
    @Mock
    private EchoServiceProperties properties;
    @InjectMocks
    private EchoServiceWebClient echoServiceWebClient;

    @BeforeEach
    void setup() {
        echoServiceWebClient = new EchoServiceWebClient(webClient, properties);
        ReflectionTestUtils.setField(echoServiceWebClient, "endpointTimeout", Duration.ofSeconds(2));
        ReflectionTestUtils.setField(echoServiceWebClient, "healthCheckTimeout", Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("echo - successful request returns response")
    void echo() {
        when(properties.endpoint()).thenReturn(ENDPOINT);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        var expectedResponse = TEST_REQUEST;
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        var response = echoServiceWebClient.echo(BASE_URL, TEST_REQUEST);

        assertEquals(expectedResponse, response, "response");
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(BASE_URL + ENDPOINT);
    }

    @Test
    @DisplayName("echo - timeout")
    void echoTimeout() {
        when(properties.endpoint()).thenReturn(ENDPOINT);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.never());

        var exception = assertThrows(EchoServiceException.class,
                () -> echoServiceWebClient.echo(BASE_URL, TEST_REQUEST));

        assertTrue(exception.getMessage().contains("timeout after 2 seconds"), "timeout message");
    }

    @Test
    @DisplayName("echo - runtime exception")
    void echoRuntimeException() {
        when(properties.endpoint()).thenReturn(ENDPOINT);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("connection failed")));

        var exception = assertThrows(EchoServiceException.class,
                () -> echoServiceWebClient.echo(BASE_URL, TEST_REQUEST));

        assertTrue(exception.getMessage().contains("request failed"), "exception message");
    }

    @Test
    @DisplayName("health check - successful response")
    void healthCheck() {
        when(properties.healthEndpoint()).thenReturn(HEALTH_ENDPOINT);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(BASE_URL + HEALTH_ENDPOINT)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(clientResponse));
        when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);

        boolean isHealthy = echoServiceWebClient.healthCheck(BASE_URL);

        assertTrue(isHealthy, "health check should return true");
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(BASE_URL + HEALTH_ENDPOINT);
        verify(clientResponse).statusCode();
    }

    @Test
    @DisplayName("health check - non-OK response status")
    void healthCheckNonOkStatus() {
        when(properties.healthEndpoint()).thenReturn(HEALTH_ENDPOINT);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(BASE_URL + HEALTH_ENDPOINT)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(clientResponse));
        when(clientResponse.statusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        boolean isHealthy = echoServiceWebClient.healthCheck(BASE_URL);

        assertFalse(isHealthy, "health check should return false");
        verify(clientResponse).statusCode();
    }

    @Test
    @DisplayName("health check - timeout")
    void healthCheckTimeout() {

        when(properties.healthEndpoint()).thenReturn(HEALTH_ENDPOINT);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(BASE_URL + HEALTH_ENDPOINT)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.never());

        boolean isHealthy = echoServiceWebClient.healthCheck(BASE_URL);

        assertFalse(isHealthy, "health check should return false");
    }

    @Test
    @DisplayName("healthCheck - exception")
    void healthCheckException() {

        when(properties.healthEndpoint()).thenReturn(HEALTH_ENDPOINT);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(BASE_URL + HEALTH_ENDPOINT)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.error(new RuntimeException("connection failed")));

        boolean isHealthy = echoServiceWebClient.healthCheck(BASE_URL);

        assertFalse(isHealthy, "health check should return false");
    }
}
