package com.badsector.anakronik.repository;
// ... importlar ...

import com.badsector.anakronik.model.ChatMessage;
import com.badsector.anakronik.model.HistoricalFigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByHistoricalFigureOrderByCreatedAtAsc(HistoricalFigure figure);
    @Transactional
    @Modifying
    void deleteByHistoricalFigure(HistoricalFigure historicalFigure);
}