package com.genezeiniss.round_robin_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "echo-service")
public record EchoServiceProperties(List<String> instances,
                                    String endpoint,
                                    String healthEndpoint) {}
