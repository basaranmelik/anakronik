package com.badsector.anakronik.service;

import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.gateway.RagServiceGatewayImpl;
import com.badsector.anakronik.gateway.dto.ask.ChatMessageDto;
import com.badsector.anakronik.gateway.dto.ask.ChatRequest;
import com.badsector.anakronik.gateway.dto.ask.ChatResponse;
import com.badsector.anakronik.gateway.dto.ask.FullChatResponse;
import com.badsector.anakronik.mapper.ChatMessageMapper;
import com.badsector.anakronik.model.ChatMessage;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.SenderType;
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

    public ChatService(UserRepository uRepo, HistoricalFigureRepository hfRepo, ChatMessageRepository cmRepo,
            RagServiceGatewayImpl gateway, ChatMessageMapper cmMapper) {
        this.userRepository = uRepo;
        this.historicalFigureRepository = hfRepo;
        this.chatMessageRepository = cmRepo;
        this.ragServiceGateway = gateway;
        this.chatMessageMapper = cmMapper;
    }

    @Transactional
    public FullChatResponse askQuestion(Long figureId, String question, String currentUserEmail) {
        User user = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = findById(figureId);
        log.info("User '{}' is asking a question to figure '{}'", currentUserEmail, figure.getName());

        saveChatMessage(figure, user, question, SenderType.USER);

        List<ChatMessage> historyForRag = chatMessageRepository.findByUserAndHistoricalFigureOrderByCreatedAtAsc(user,
                figure);
        List<ChatMessageDto> historyDtoForRag = historyForRag.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        ChatRequest chatRequest = new ChatRequest(user.getId(), figure.getId(), figure.getName(), question,
                historyDtoForRag);

        log.info("Sending question to RAG service for figureId: {}", figure.getId());
        ChatResponse response;
        try {
            response = ragServiceGateway.askQuestion(chatRequest);
        } catch (Exception e) {
            log.error("Failed to get answer from RAG service for figureId: {}. Error: {}", figure.getId(),
                    e.getMessage());
            throw new RuntimeException("Error while communicating with the chat service.", e);
        }
        log.info("Received answer from RAG service for figure '{}'", figure.getName());

        saveChatMessage(figure, user, response.answer(), SenderType.FIGURE);

        List<ChatMessage> finalHistory = chatMessageRepository.findByUserAndHistoricalFigureOrderByCreatedAtAsc(user,
                figure);
        List<ChatMessageDto> finalHistoryDto = finalHistory.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        return new FullChatResponse(response.answer(), finalHistoryDto);
    }

    public FullChatResponse getChatHistory(Long figureId, String username) {
        User user = findUserByEmail(username);
        HistoricalFigure figure = findById(figureId);

        List<ChatMessage> history = chatMessageRepository.findByUserAndHistoricalFigureOrderByCreatedAtAsc(user,
                figure);

        List<ChatMessageDto> chatMessageDtos = history.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        return new FullChatResponse(null, chatMessageDtos);
    }

    @Transactional
    public void clearChatHistory(Long figureId, String currentUserEmail) {
        User user = findUserByEmail(currentUserEmail);
        HistoricalFigure figure = findById(figureId);
        log.info("Clearing chat history for figure '{}' (id: {}) by user '{}'", figure.getName(), figureId,
                currentUserEmail);

        chatMessageRepository.deleteByUserAndHistoricalFigure(user, figure);

        log.info("Successfully cleared chat history for figure '{}'", figure.getName());
    }

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
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private HistoricalFigure findById(Long figureId) {
        return historicalFigureRepository.findById(figureId)
                .orElseThrow(() -> new ResourceNotFoundException("Historical Figure not found with id: " + figureId));
    }
}