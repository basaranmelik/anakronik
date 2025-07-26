package com.badsector.anakronik.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historical_figure_id", nullable = false)
    private HistoricalFigure historicalFigure;

    @Column(name = "doc_name", nullable = false)
    private String docName;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Document() {
    }

    public Document(Long id, HistoricalFigure historicalFigure, String docName, String filePath, Instant createdAt) {
        this.id = id;
        this.historicalFigure = historicalFigure;
        this.docName = docName;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HistoricalFigure getHistoricalFigure() {
        return historicalFigure;
    }

    public void setHistoricalFigure(HistoricalFigure historicalFigure) {
        this.historicalFigure = historicalFigure;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
