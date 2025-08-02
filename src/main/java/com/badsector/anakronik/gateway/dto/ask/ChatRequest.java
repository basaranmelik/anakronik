package com.badsector.anakronik.gateway.dto.ask;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChatRequest(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("historical_figure_id") Long historicalFigureId,
        @JsonProperty("historical_figure_name") String historicalFigureName,
        @JsonProperty("question") String question,
        @JsonProperty("history") List<ChatMessageDto> history
) {}