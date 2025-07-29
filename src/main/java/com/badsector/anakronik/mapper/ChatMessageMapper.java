package com.badsector.anakronik.mapper;

import com.badsector.anakronik.gateway.dto.ask.ChatMessageDto;
import com.badsector.anakronik.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {
    public ChatMessageDto toDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }

        return new ChatMessageDto(
                chatMessage.getSender().name(),
                chatMessage.getMessage()
        );
    }
}