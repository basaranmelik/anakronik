package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.AuthResponse;
import com.badsector.anakronik.controller.dto.LoginRequest;
import com.badsector.anakronik.controller.dto.RegisterRequest;
import com.badsector.anakronik.controller.dto.RefreshTokenRequest;
import com.badsector.anakronik.controller.dto.TokenRefreshResponse;
import com.badsector.anakronik.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend.login-url}")
    private String frontendLoginUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String responseMessage = authService.register(request);
        return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public RedirectView verifyAccount(@RequestParam("token") String token) {
        authService.verifyUser(token);
        String redirectUrlWithStatus = frontendLoginUrl + "?verified=true";

        return new RedirectView(redirectUrlWithStatus);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("User logged out successfully.");
    }
}