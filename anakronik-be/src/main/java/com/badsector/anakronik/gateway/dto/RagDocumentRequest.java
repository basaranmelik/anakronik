package com.badsector.anakronik.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

public record RagDocumentRequest(
        @JsonProperty("file") Path file,
        @JsonProperty("historical_figure_id")Long historicalFigureId,
        @JsonProperty("historical_figure_name")String historicalFigureName,
        @JsonProperty("user_id")Long userId
) {}