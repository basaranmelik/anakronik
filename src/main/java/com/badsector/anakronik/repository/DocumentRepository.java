package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.Document;
import com.badsector.anakronik.model.HistoricalFigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Transactional
    void deleteByHistoricalFigure(HistoricalFigure historicalFigure);

}