package com.badsector.anakronik.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_figure_id", nullable = false)
    private HistoricalFigure historicalFigure;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(name = "sender", nullable = false, length = 50)
    private SenderType sender;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HistoricalFigure getHistoricalFigure() {
        return historicalFigure;
    }

    public void setHistoricalFigure(HistoricalFigure historicalFigure) {
        this.historicalFigure = historicalFigure;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SenderType getSender() {
        return sender;
    }

    public void setSender(SenderType sender) {
        this.sender = sender;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
