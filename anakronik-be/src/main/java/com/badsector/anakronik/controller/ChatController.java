package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.ChatQuestionRequest;
import com.badsector.anakronik.gateway.dto.ask.FullChatResponse;
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
    public ResponseEntity<FullChatResponse> askQuestion(
            @PathVariable Long figureId,
            @Valid @RequestBody ChatQuestionRequest request,
            Authentication authentication) {
        FullChatResponse response = chatService.askQuestion(
                figureId,
                request.question(),
                authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{figureId}")
    public ResponseEntity<FullChatResponse> getChatHistory(
            @PathVariable Long figureId,
            Authentication authentication) {
        FullChatResponse response = chatService.getChatHistory(
                figureId,
                authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{figureId}")
    public ResponseEntity<Void> clearChatHistory(
            @PathVariable Long figureId,
            Authentication authentication) {
        chatService.clearChatHistory(figureId, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}