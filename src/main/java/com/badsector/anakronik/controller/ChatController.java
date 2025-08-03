package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.ChatQuestionRequest;
import com.badsector.anakronik.gateway.dto.ask.FullChatResponse; // <-- Yeni DTO'yu import et
import com.badsector.anakronik.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{figureId}")
    public ResponseEntity<FullChatResponse> askQuestion( // <-- 1. Dönüş tipini değiştir
                                                         @PathVariable Long figureId,
                                                         @Valid @RequestBody ChatQuestionRequest request,
                                                         Authentication authentication
    ) {
        // 2. Servisten dönecek nesnenin tipini güncelle
        FullChatResponse response = chatService.askQuestion(
                figureId,
                request.question(),
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> clearChatHistory(
            @PathVariable Long figureId,
            Authentication authentication
    ) {
        chatService.clearChatHistory(figureId, authentication.getName());

        // Başarılı silme işlemleri için genellikle 204 No Content yanıtı döndürülür.
        return ResponseEntity.noContent().build();
    }
}