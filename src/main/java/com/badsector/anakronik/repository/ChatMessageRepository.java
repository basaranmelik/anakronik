package com.badsector.anakronik.repository;
// ... importlar ...

import com.badsector.anakronik.model.ChatMessage;
import com.badsector.anakronik.model.HistoricalFigure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Metot adı yeni alan adlarına göre güncellendi.
    List<ChatMessage> findByHistoricalFigureOrderByCreatedAtAsc(HistoricalFigure figure);
}