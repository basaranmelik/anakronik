package com.badsector.anakronik.repository;

import com.badsector.anakronik.model.RefreshToken;
import com.badsector.anakronik.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}