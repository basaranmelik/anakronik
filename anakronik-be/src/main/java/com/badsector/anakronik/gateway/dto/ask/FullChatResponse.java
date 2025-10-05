package com.badsector.anakronik.gateway.dto.ask;

import java.util.List;

public record FullChatResponse(
        String answer,
        List<ChatMessageDto> history
) {}