package com.genezeiniss.round_robin_service.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genezeiniss.round_robin_service.configuration.GlobalExceptionHandler;
import com.genezeiniss.round_robin_service.exception.NoHealthyInstancesException;
import com.genezeiniss.round_robin_service.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoundRobinControllerTest {

    private static final Map<String, Object> TEST_REQUEST = Map.of(
            "game", "Mobile Legends",
            "gamerID", "GYUTDTE",
            "points", 20);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Mock
    private RouteService routeService;
    @InjectMocks
    private RoundRobinController roundRobinController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(roundRobinController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("echo - successfully response")
    void echo() throws Exception {

        when(routeService.routeRequest(TEST_REQUEST)).thenReturn(TEST_REQUEST);

        postEcho()
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game").value("Mobile Legends"))
                .andExpect(jsonPath("$.gamerID").value("GYUTDTE"))
                .andExpect(jsonPath("$.points").value(20));
    }

    @Test
    @DisplayName("echo - no healthy instances")
    void echoNoHealthyInstances() throws Exception {

        var errorMessage = "No available echo-service instances";

        when(routeService.routeRequest(TEST_REQUEST)).thenThrow(new NoHealthyInstancesException(errorMessage));

        postEcho()
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    private ResultActions postEcho() throws Exception {
        return mockMvc.perform(post("/api/round-robin/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TEST_REQUEST)));
    }
}
