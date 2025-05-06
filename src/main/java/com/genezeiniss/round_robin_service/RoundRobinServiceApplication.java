package com.genezeiniss.round_robin_service;

import com.genezeiniss.round_robin_service.configuration.EchoServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(EchoServiceProperties.class)
public class RoundRobinServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoundRobinServiceApplication.class, args);
	}
}
