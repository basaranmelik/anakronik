package com.badsector.anakronik.gateway.dto.ask;

import com.fasterxml.jackson.annotation.JsonProperty;

// Python'daki /ask endpoint'inden dönecek yanıtı temsil eder.
public record ChatResponse(
        @JsonProperty("answer") String answer
) {}