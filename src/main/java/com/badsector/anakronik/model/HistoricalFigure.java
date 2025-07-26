package com.badsector.anakronik.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import edin

@Entity
@Table(name = "historical_figures")
public class HistoricalFigure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private LocalDate birthDate;

    private LocalDate deathDate;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore // <<< Bu satırı ekleyin
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Bu tarihi kişilikle ilişkili belgeler
    @OneToMany(mappedBy = "historicalFigure", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <<< Bu satırı da ekleyin, HistoricalFigure listelenirken Document'ları göstermemek için
    private List<Document> documents;

    // Bu tarihi kişilikle ilişkili chat mesajları
    @OneToMany(mappedBy = "historicalFigure", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <<< Bu satırı da ekleyin, HistoricalFigure listelenirken ChatMessage'ları göstermemek için
    private List<ChatMessage> chatMessages;

    public HistoricalFigure() {
    }

    public HistoricalFigure(Long id, String name, LocalDate birthDate, LocalDate deathDate, String bio, User createdBy, Instant createdAt, List<Document> documents, List<ChatMessage> chatMessages) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.bio = bio;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.documents = documents;
        this.chatMessages = chatMessages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }
}
