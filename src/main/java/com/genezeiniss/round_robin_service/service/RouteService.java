package com.genezeiniss.round_robin_service.service;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import com.genezeiniss.round_robin_service.exception.NoHealthyInstancesException;
import com.genezeiniss.round_robin_service.rest.EchoServiceWebClient;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final EchoServiceProperties echoServiceProperties;
    private final EchoServiceWebClient echoServiceWebClient;

    @Getter
    private final Set<String> unhealthyInstances = ConcurrentHashMap.newKeySet();
    @Getter
    private final Queue<String> instanceQueue = new ConcurrentLinkedQueue<>();

    @PostConstruct
    protected void init() {
        instanceQueue.addAll(echoServiceProperties.instances());
    }

    public Map<String, Object> routeRequest(Map<String, Object> request) {

        int attempts = instanceQueue.size();

        while (attempts-- > 0) {
            String instance = instanceQueue.poll();
            try {
                var response = echoServiceWebClient.echo(instance, request);
                instanceQueue.offer(instance);
                return response;
            } catch (Exception exception) {
                log.error("Route to instance {} failed. Continue to the next instance", instance, exception);
                unhealthyInstances.add(instance);
            }
        }
        throw new NoHealthyInstancesException("No available echo-service instances");
    }

    protected void reinstateInstance(String instance){
        if(unhealthyInstances.remove(instance)) {
            instanceQueue.offer(instance);
            log.info("Reinstated instance {}", instance);
        }
    }
}
