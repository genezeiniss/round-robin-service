package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final EchoServiceWebClient echoServiceWebClient;
    private final RouteService routeService;

    @Scheduled(fixedRate = 30_000)
    public void checkHealth() {
        routeService.unhealthyInstances.forEach(instance -> {
            if (echoServiceWebClient.health(instance)) {
                routeService.updateHealthyInstances(instance);
            }
        });
    }
}
