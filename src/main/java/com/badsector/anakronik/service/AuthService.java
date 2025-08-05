package com.badsector.anakronik.service;

import com.badsector.anakronik.controller.dto.*;
import com.badsector.anakronik.model.PasswordResetToken;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.UserRole;
import com.badsector.anakronik.model.VerificationToken;
import com.badsector.anakronik.repository.PasswordResetTokenRepository;
import com.badsector.anakronik.repository.UserRepository;
import com.badsector.anakronik.repository.VerificationTokenRepository;
import com.badsector.anakronik.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService,
                       VerificationTokenRepository verificationTokenRepository,
                       EmailService emailService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Bu e-posta adresi zaten kullanımda!");
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

    public String verifyUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Geçersiz doğrulama token'ı."));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            userRepository.delete(verificationToken.getUser());
            throw new RuntimeException("Doğrulama token'ının süresi dolmuş. Lütfen tekrar kayıt olun.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken); // Kullanılmış token'ı sil

        return "Hesabınız başarıyla doğrulandı. Artık giriş yapabilirsiniz.";
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        if (!user.isEnabled()) {
            throw new IllegalStateException("Giriş yapmadan önce hesabınızı doğrulamanız gerekmektedir.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        return new AuthResponse(accessToken, refreshToken);
    }

    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.refreshToken();
        logger.info("Token yenileme isteği alındı. Token: {}", requestToken);

        return refreshTokenService.findByToken(requestToken)
                .map(refreshToken -> {
                    logger.info("Refresh token veritabanında bulundu.");
                    return refreshTokenService.verifyExpiration(refreshToken);
                })
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    logger.info("Token'a ait kullanıcı bulundu: {}", user.getEmail());
                    logger.info("Kullanıcının 'enabled' durumu: {}", user.isEnabled());

                    if (!user.isEnabled()) {
                        logger.error("KULLANICI AKTİF DEĞİL! Kullanıcı: {}, Enabled: {}", user.getEmail(), user.isEnabled());
                        throw new IllegalStateException("Kullanıcı hesabı aktif değil. Token yenilenemedi.");
                    }

                    logger.info("Kullanıcı aktif. Yeni access token üretiliyor...");
                    String accessToken = jwtService.generateToken(user);
                    logger.info("Yeni access token başarıyla üretildi.");

                    return new TokenRefreshResponse(accessToken);
                })
                .orElseThrow(() -> {
                    logger.error("Refresh token veritabanında bulunamadı! Token: {}", requestToken);
                    return new RuntimeException("Refresh token veritabanında bulunamadı!");
                });
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }

    // --- ŞİFRE SIFIRLAMA METOTLARI ---

    public void processForgotPassword(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Bu e-posta adresine sahip kullanıcı bulunamadı."));

        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void processResetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Geçersiz şifre sıfırlama token'ı."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalStateException("Token'ın süresi dolmuş. Lütfen yeni bir sıfırlama talebi oluşturun.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
    public void changePassword(ChangePasswordRequest request, String username) {

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Oturum açmış kullanıcı bulunamadı: " + username));

        if (!passwordEncoder.matches(request.oldPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("Girilen eski şifre yanlış.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }
}