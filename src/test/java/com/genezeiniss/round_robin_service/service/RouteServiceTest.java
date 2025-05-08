package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import com.genezeiniss.round_robin_service.exception.NoHealthyInstancesException;
import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest {

    private static final Map<String, Object> TEST_REQUEST = Map.of(
            "game", "Mobile Legends",
            "gamerID", "GYUTDTE",
            "points", 20);
    private final String INSTANCE_1 = "localhost:3001";
    private final String INSTANCE_2 = "localhost:3002";
    private final String INSTANCE_3 = "localhost:3003";
    @Mock
    private EchoServiceProperties echoServiceProperties;
    @Mock
    private EchoServiceWebClient echoServiceWebClient;
    @InjectMocks
    private RouteService routeService;

    @BeforeEach
    void setup() {
        when(echoServiceProperties.instances())
                .thenReturn(List.of(INSTANCE_1, INSTANCE_2, INSTANCE_3));
        routeService.init();
        ReflectionTestUtils.setField(routeService, "retries", 4);
    }

    @Test
    @DisplayName("route request - send single request when all service instances are healthy")
    void routeRequest() {

        when(echoServiceWebClient.echo(INSTANCE_1, TEST_REQUEST)).thenReturn(TEST_REQUEST);

        var response = routeService.routeRequest(TEST_REQUEST);

        assertEquals(TEST_REQUEST, response, "response");
        assertTrue(routeService.getUnhealthyInstances().isEmpty(), "unhealthy instances set is empty");
        assertEquals(INSTANCE_2, routeService.getInstanceQueue().element(), "next instance in a head of queue");
        assertEquals(3, routeService.getInstanceQueue().size(), "queue size");
    }

    @Test
    @DisplayName("route request - send multiple requests when all service instances are healthy")
    void routeRequestMultipleRequests() {

        var expectedSequence = List.of(INSTANCE_1, INSTANCE_2, INSTANCE_3);

        expectedSequence.forEach(instance -> {
            when(echoServiceWebClient.echo(instance, TEST_REQUEST)).thenReturn(TEST_REQUEST);
            routeService.routeRequest(TEST_REQUEST);
            verify(echoServiceWebClient).echo(instance, TEST_REQUEST);
        });

        verifyNoMoreInteractions(echoServiceWebClient);
    }

    @Test
    @DisplayName("route request - unhealthy instance is skipped")
    void routeRequestUnhealthyInstance() {

        when(echoServiceWebClient.echo(INSTANCE_1, TEST_REQUEST)).thenThrow(new RuntimeException());
        when(echoServiceWebClient.echo(INSTANCE_2, TEST_REQUEST)).thenReturn(TEST_REQUEST);

        var response = routeService.routeRequest(TEST_REQUEST);

        assertEquals(TEST_REQUEST, response, "response");
        assertEquals(1, routeService.getUnhealthyInstances().size(), "unhealthy instances set size");
        assertTrue(routeService.getUnhealthyInstances().contains(INSTANCE_1), "unhealthy instances set contains instance 1");
        assertEquals(INSTANCE_3, routeService.getInstanceQueue().element(), "next instance in a head of queue");
        assertEquals(2, routeService.getInstanceQueue().size(), "queue size");
    }

    @Test
    @DisplayName("route request - all instances unhealthy")
    void routeRequestAllInstancesUnhealthy() {

        when(echoServiceWebClient.echo(anyString(), eq(TEST_REQUEST))).thenThrow(new RuntimeException());

        var exception = assertThrows(NoHealthyInstancesException.class,
                () -> routeService.routeRequest(TEST_REQUEST), "expected exception");

        assertEquals("No available echo-service instances", exception.getMessage(), "exception message");
        assertEquals(3, routeService.getUnhealthyInstances().size(), "unhealthy instances set size");
        assertTrue(routeService.getInstanceQueue().isEmpty(), "queue is empty");
    }

    @Test
    @DisplayName("reinstate instance - moves instance from unhealthy set back to queue")
    void reinstateInstance() {

        when(echoServiceWebClient.echo(INSTANCE_1, TEST_REQUEST)).thenThrow(new RuntimeException());
        routeService.routeRequest(TEST_REQUEST);

        assertEquals(1, routeService.getUnhealthyInstances().size(), "unhealthy instances set size");
        assertEquals(2, routeService.getInstanceQueue().size(), "queue size");

        routeService.reinstateInstance(INSTANCE_1);

        assertEquals(0, routeService.getUnhealthyInstances().size(), "unhealthy instances set size");
        assertEquals(3, routeService.getInstanceQueue().size(), "queue size");
        assertFalse(routeService.getUnhealthyInstances().contains(INSTANCE_1), "instance no longer in unhealthy set");
    }

    @Test
    @DisplayName("reinstate instance - unknown instance does nothing")
    void reinstateInstanceUnknownInstance() {

        var unknownInstance = "localhost:9999";
        routeService.reinstateInstance(unknownInstance);

        assertEquals(3, routeService.getInstanceQueue().size(), "queue size");
        assertTrue(routeService.getUnhealthyInstances().isEmpty(), "unhealthy instances set is empty");
    }
}
