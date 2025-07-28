package com.badsector.anakronik.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Bu sınıf, RAG servisinden dönen yanıttaki "figure_info" nesnesini temsil eder.
public record FigureInfo(
        @JsonProperty("birth_date") String birthDate,
        @JsonProperty("death_date") String deathDate,
        @JsonProperty("region") String region
) {}