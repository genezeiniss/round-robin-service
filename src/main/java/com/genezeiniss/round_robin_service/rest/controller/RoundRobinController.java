package com.genezeiniss.round_robin_service.rest.controller;

import com.genezeiniss.round_robin_service.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/round-robin")
public class RoundRobinController {

    private final RouteService routeService;

    @PostMapping("/echo")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> echo(@RequestBody Map<String, Object> request) {
        return routeService.echo(request);
    }
}
