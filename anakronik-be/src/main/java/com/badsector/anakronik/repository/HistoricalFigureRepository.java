package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalFigureRepository extends JpaRepository<HistoricalFigure, Long> {
    Page<HistoricalFigure> findByCreatedBy(User user, Pageable pageable);
    List<HistoricalFigure> findByCreatedBy(User user);
    Optional<HistoricalFigure> findByIdAndCreatedBy(Long id, User user);
    @Query("SELECT hf FROM HistoricalFigure hf WHERE hf.createdBy = :user OR hf.createdBy.role = :adminRole")
    Page<HistoricalFigure> findFiguresForUserView(@Param("user") User user, @Param("adminRole") UserRole adminRole, Pageable pageable);
    boolean existsByNameAndCreatedBy(String name, User user);
}