package com.badsector.anakronik.mapper;

import com.badsector.anakronik.gateway.dto.ask.ChatMessageDto;
import com.badsector.anakronik.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    /**
     * ChatMessage veritabanı nesnesini, dış servisin anlayacağı DTO formatına çevirir.
     * @param chatMessage Veritabanından gelen entity.
     * @return Dış servise gönderilecek DTO.
     */
    public ChatMessageDto toDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }

        // ChatMessageDto'nun record constructor'ını kullanarak yeni nesneyi oluşturuyoruz.
        // DTO'daki 'role' alanı, entity'deki 'sender' enum'ından gelir.
        // DTO'daki 'content' alanı, entity'deki 'message' alanından gelir.
        return new ChatMessageDto(
                chatMessage.getSender().name(), // .name() metodu enum'ı String'e çevirir (örn: SenderType.USER -> "USER")
                chatMessage.getMessage()
        );
    }
}