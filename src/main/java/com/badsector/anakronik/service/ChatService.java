package com.badsector.anakronik.service;

import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.ask.ChatMessageDto;
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
import com.badsector.anakronik.mapper.ChatMessageMapper;
import com.badsector.anakronik.model.ChatMessage; // Değiştirildi
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.SenderType; // Değiştirildi
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.repository.ChatMessageRepository;
import com.badsector.anakronik.repository.HistoricalFigureRepository;
import com.badsector.anakronik.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final UserRepository userRepository;
    private final HistoricalFigureRepository historicalFigureRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagServiceGatewayImpl ragServiceGateway;
    private final ChatMessageMapper chatMessageMapper;

    public ChatService(UserRepository uRepo, HistoricalFigureRepository hfRepo, ChatMessageRepository cmRepo, RagServiceGatewayImpl gateway, ChatMessageMapper cmMapper) {
        this.userRepository = uRepo;
        this.historicalFigureRepository = hfRepo;
        this.chatMessageRepository = cmRepo;
        this.ragServiceGateway = gateway;
        this.chatMessageMapper = cmMapper;
    }

    @Transactional
    public ChatResponse askQuestion(Long figureId, String question, String currentUserEmail) {
        User user = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = findByIdAndUser(figureId, user);
        log.info("User '{}' is asking a question to figure '{}'", currentUserEmail, figure.getName());

        saveChatMessage(figure, user, question, SenderType.USER); // Değiştirildi

        // Repository metot adı güncellendi
        List<ChatMessage> chatHistory = chatMessageRepository.findByHistoricalFigureOrderByCreatedAtAsc(figure);
        List<ChatMessageDto> historyDto = chatHistory.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        ChatRequest chatRequest = new ChatRequest(user.getId(), figure.getId(), figure.getName(), question, historyDto);

        log.info("Sending question to RAG service for figureId: {}", figure.getId());
        ChatResponse response;
        try {
            response = ragServiceGateway.askQuestion(chatRequest);
        } catch (Exception e) {
            log.error("Failed to get answer from RAG service for figureId: {}. Error: {}", figure.getId(), e.getMessage());
            throw new RuntimeException("Error while communicating with the chat service.", e);
        }
        log.info("Received answer from RAG service for figure '{}'", figure.getName());

        saveChatMessage(figure, user, response.answer(), SenderType.FIGURE); // Değiştirildi

        return response;
    }

    // --- Yardımcı Metotlar ---
    private void saveChatMessage(HistoricalFigure figure, User user, String message, SenderType sender) { // Değiştirildi
        ChatMessage chatMessage = new ChatMessage(); // Değiştirildi
        chatMessage.setHistoricalFigure(figure);
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setSender(sender); // Değiştirildi
        chatMessage.setCreatedAt(Instant.now()); // Değiştirildi
        chatMessageRepository.save(chatMessage);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private HistoricalFigure findByIdAndUser(Long figureId, User user) {
        return historicalFigureRepository.findByIdAndCreatedBy(figureId, user).orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
    }
}