package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.RefreshToken;
import com.badsector.anakronik.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    @Transactional
    void deleteByToken(String token);
}