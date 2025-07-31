package com.badsector.anakronik.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FigureInfo(
        @JsonProperty("birth_date") String birthDate,
        @JsonProperty("death_date") String deathDate,
        @JsonProperty("region") String region,
        @JsonProperty("bio") String bio

) {}