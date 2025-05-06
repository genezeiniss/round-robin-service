package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final EchoServiceProperties echoServiceProperties;
    private final EchoServiceWebClient echoServiceWebClient;
    @Getter
    protected final Set<String> unhealthyInstances = ConcurrentHashMap.newKeySet();

    protected Queue<String> echoServiceInstances;

    @PostConstruct
    private void init() {
        echoServiceInstances = new ConcurrentLinkedQueue<>(echoServiceProperties.getInstances());
    }

    public Map<String, Object> echo(Map<String, Object> request) {

        int maxAttempts = echoServiceInstances.size();

        while (maxAttempts > 0) {
            String instance = echoServiceInstances.poll();
            try {
                var response = echoServiceWebClient.echo(instance, request);
                echoServiceInstances.offer(instance);
                return response;
            } catch (Exception exception) {
                log.error("Route to instance {} failed. Continue to the next instance", instance, exception);
                unhealthyInstances.add(instance);
                maxAttempts--;
            }
        }
        throw new RuntimeException("No available echo-service instances");
    }

    protected void updateHealthyInstances(String instance){
        unhealthyInstances.remove(instance);
        echoServiceInstances.offer(instance);
        log.info("Instance {} is healthy", instance);
    }
}
