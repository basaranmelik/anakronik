package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User; // User modelini import edin
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalFigureRepository extends JpaRepository<HistoricalFigure, Long> {

    // Mevcut metot (kullanmaya devam edeceğiz)
    List<HistoricalFigure> findByCreatedBy(User user);

    // YENİ: Belirli bir kullanıcıya ait kayıtları sayfalı getirmek için
    Page<HistoricalFigure> findByCreatedBy(User user, Pageable pageable);

    // YENİ ve ÇOK ÖNEMLİ: Belirli bir ID'ye sahip ve AYNI ZAMANDA belirli bir kullanıcıya ait olan kaydı bulur.
    // Bu metot, sahiplik kontrolü için kilit rol oynayacak.
    Optional<HistoricalFigure> findByIdAndCreatedBy(Long id, User user);
}
