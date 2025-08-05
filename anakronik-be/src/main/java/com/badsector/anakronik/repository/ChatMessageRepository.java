package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.ChatMessage;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User; // User import'u ekleyin
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying; // Gerekli import
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserAndHistoricalFigureOrderByCreatedAtAsc(User user, HistoricalFigure figure);
    @Transactional
    @Modifying
    void deleteByUserAndHistoricalFigure(User user, HistoricalFigure historicalFigure);
    @Transactional
    @Modifying
    void deleteByHistoricalFigure(HistoricalFigure historicalFigure);
}