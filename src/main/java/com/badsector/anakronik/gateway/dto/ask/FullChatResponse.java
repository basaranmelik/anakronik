package com.badsector.anakronik.gateway.dto.ask;

import java.util.List;

// Java 17+ record'ları bu tarz DTO'lar için idealdir.
public record FullChatResponse(
        String answer,
        List<ChatMessageDto> history
) {}