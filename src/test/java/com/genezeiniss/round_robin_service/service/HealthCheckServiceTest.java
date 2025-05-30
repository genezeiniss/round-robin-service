package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthCheckServiceTest {

    private static final String INSTANCE_1 = "localhost:3001";
    private static final String INSTANCE_2 = "localhost:3002";
    @Mock
    private EchoServiceWebClient echoServiceWebClient;
    @Mock
    private RouteService routeService;
    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    @DisplayName("check health - no unhealthy instances")
    void checkHealthNoUnhealthyInstances() {

        when(routeService.getUnhealthyInstances()).thenReturn(Set.of());

        healthCheckService.checkHealth();

        verifyNoInteractions(echoServiceWebClient);
        verify(routeService, never()).reinstateInstance(any());
    }

    @Test
    @DisplayName("check health - healthy instance gets reinstated")
    void checkHealthHealthyInstance() {

        when(routeService.getUnhealthyInstances()).thenReturn(Set.of(INSTANCE_1));
        when(echoServiceWebClient.healthCheck(INSTANCE_1)).thenReturn(true);

        healthCheckService.checkHealth();

        verify(routeService, timeout(100)).reinstateInstance(INSTANCE_1);
        verifyNoMoreInteractions(echoServiceWebClient, routeService);
    }

    @Test
    @DisplayName("check health - unhealthy instance remains unhealthy")
    void checkHealthUnhealthyInstance() {
        when(routeService.getUnhealthyInstances()).thenReturn(Set.of(INSTANCE_1));
        when(echoServiceWebClient.healthCheck(INSTANCE_1)).thenReturn(false);

        healthCheckService.checkHealth();

        verify(routeService, never()).reinstateInstance(any());
    }

    @Test
    @DisplayName("check health - multiple instances handles all")
    void checkHealthMultipleInstances() {
        when(routeService.getUnhealthyInstances()).thenReturn(Set.of(INSTANCE_1, INSTANCE_2));
        when(echoServiceWebClient.healthCheck(INSTANCE_1)).thenReturn(true);
        when(echoServiceWebClient.healthCheck(INSTANCE_2)).thenReturn(false);

        healthCheckService.checkHealth();

        verify(routeService, timeout(100)).reinstateInstance(INSTANCE_1);
        verify(routeService, never()).reinstateInstance(INSTANCE_2);
    }
}
