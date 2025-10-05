package com.badsector.anakronik.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagUploadResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("figure_info") FigureInfo figureInfo
) {}