package com.badsector.anakronik.gateway.dto.ask;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(
        @JsonProperty("answer") String answer
) {}