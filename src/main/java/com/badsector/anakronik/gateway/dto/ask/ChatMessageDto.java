package com.badsector.anakronik.gateway.dto.ask;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatMessageDto(
        @JsonProperty("role") String role,
        @JsonProperty("content") String content
) {}