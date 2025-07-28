package com.badsector.anakronik.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Bu sınıf, RAG servisinden gelen tam JSON yanıtını temsil eder.
public record RagUploadResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("figure_info") FigureInfo figureInfo
) {}