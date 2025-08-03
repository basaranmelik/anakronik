package com.badsector.anakronik.service;

import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.ask.ChatMessageDto;
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
import com.badsector.anakronik.gateway.dto.ask.FullChatResponse;
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
    public FullChatResponse askQuestion(Long figureId, String question, String currentUserEmail) { // <-- 1. Dönüş tipini değiştir
        User user = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = findByIdAndUser(figureId, user);
        log.info("User '{}' is asking a question to figure '{}'", currentUserEmail, figure.getName());

        saveChatMessage(figure, user, question, SenderType.USER);

        // RAG servisine göndermek için geçmişi al
        List<ChatMessage> historyForRag = chatMessageRepository.findByHistoricalFigureOrderByCreatedAtAsc(figure);
        List<ChatMessageDto> historyDtoForRag = historyForRag.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        ChatRequest chatRequest = new ChatRequest(user.getId(), figure.getId(), figure.getName(), question, historyDtoForRag);

        log.info("Sending question to RAG service for figureId: {}", figure.getId());
        ChatResponse response;
        try {
            response = ragServiceGateway.askQuestion(chatRequest);
        } catch (Exception e) {
            log.error("Failed to get answer from RAG service for figureId: {}. Error: {}", figure.getId(), e.getMessage());
            throw new RuntimeException("Error while communicating with the chat service.", e);
        }
        log.info("Received answer from RAG service for figure '{}'", figure.getName());

        // AI'ın cevabını da kaydet
        saveChatMessage(figure, user, response.answer(), SenderType.FIGURE);

        // Frontend'e göndermek için TÜM geçmişi (AI'ın yeni cevabı dahil) tekrar çek
        List<ChatMessage> finalHistory = chatMessageRepository.findByHistoricalFigureOrderByCreatedAtAsc(figure);
        List<ChatMessageDto> finalHistoryDto = finalHistory.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        // Yeni DTO'yu oluştur ve döndür
        return new FullChatResponse(response.answer(), finalHistoryDto); // <-- 2. Yeni DTO'yu döndür
    }

    @Transactional
    public void clearChatHistory(Long figureId, String currentUserEmail) {
        User user = findUserByEmail(currentUserEmail);
        // Silme işlemi yapılacak tarihi karakterin bu kullanıcıya ait olduğunu doğrula
        HistoricalFigure figure = findByIdAndUser(figureId, user);

        log.info("Clearing chat history for figure '{}' (id: {}) by user '{}'", figure.getName(), figureId, currentUserEmail);

        // Repository üzerinden silme işlemini çağır
        chatMessageRepository.deleteByHistoricalFigure(figure);

        log.info("Successfully cleared chat history for figure '{}'", figure.getName());
    }

    // --- Yardımcı Metotlar ---
    private void saveChatMessage(HistoricalFigure figure, User user, String message, SenderType sender) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setHistoricalFigure(figure);
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setSender(sender);
        chatMessage.setCreatedAt(Instant.now());
        chatMessageRepository.save(chatMessage);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private HistoricalFigure findByIdAndUser(Long figureId, User user) {
        return historicalFigureRepository.findByIdAndCreatedBy(figureId, user).orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
    }
}