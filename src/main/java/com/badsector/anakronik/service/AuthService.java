package com.badsector.anakronik.service;

import com.badsector.anakronik.controller.dto.AuthResponse;
import com.badsector.anakronik.controller.dto.LoginRequest;
import com.badsector.anakronik.controller.dto.RegisterRequest;
import com.badsector.anakronik.controller.dto.RefreshTokenRequest;
import com.badsector.anakronik.controller.dto.TokenRefreshResponse;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.model.VerificationToken;
import com.badsector.anakronik.repository.UserRepository;
import com.badsector.anakronik.repository.VerificationTokenRepository;
import com.badsector.anakronik.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already in use!");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ROLE_USER);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(Instant.now().plus(24, ChronoUnit.HOURS));
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return "Kayıt başarılı! Lütfen hesabınızı doğrulamak için e-postanızı kontrol edin.";
    }

    @Transactional
    public String verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token."));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            userRepository.delete(verificationToken.getUser());
            throw new RuntimeException("Verification token was expired. Please register again.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return "Hesabınız başarıyla doğrulandı. Artık giriş yapabilirsiniz.";
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        return new AuthResponse(accessToken, refreshToken);
    }

    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    String accessToken = jwtService.generateToken(user);
                    return new TokenRefreshResponse(accessToken);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in the database!"));
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }
}