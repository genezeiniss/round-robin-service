package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final EchoServiceWebClient echoServiceWebClient;
    private final RouteService routeService;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();


    @Scheduled(fixedRate = 30_000)
    public void checkHealth() {
        routeService.getUnhealthyInstances().forEach(instance ->
                executor.execute(() -> {
                    if (echoServiceWebClient.healthCheck(instance)) {
                        routeService.reinstateInstance(instance);
                    }
                }));
    }
}
