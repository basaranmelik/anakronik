package com.badsector.anakronik.controller;

import com.badsector.anakronik.dto.ChatQuestionRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
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

    /**
     * Belirli bir tarihi şahsiyete sohbet sorusu gönderir ve yanıtını alır.
     *
     * @param figureId Konuşulacak olan tarihi şahsiyetin ID'si.
     * @param request Kullanıcının sorusunu içeren JSON gövdesi.
     * @param authentication Mevcut giriş yapmış kullanıcının kimlik bilgileri.
     * @return Yapay zekadan gelen yanıt.
     */
    @PostMapping("/{figureId}")
    public ResponseEntity<ChatResponse> askQuestion(
            @PathVariable Long figureId,
            @Valid @RequestBody ChatQuestionRequest request,
            Authentication authentication
    ) {
        // Gelen isteği ve kullanıcı bilgisini doğrudan ChatService'e yönlendiriyoruz.
        ChatResponse response = chatService.askQuestion(
                figureId,
                request.question(),
                authentication.getName() // Spring Security, giriş yapan kullanıcının email'ini/username'ini buradan verir.
        );

        // Servisten gelen yanıtı 200 OK durumuyla istemciye döndürüyoruz.
        return ResponseEntity.ok(response);
    }
}