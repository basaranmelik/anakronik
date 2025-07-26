package com.badsector.anakronik.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import edin

@Entity
@Table(name = "users")
public class User implements org.springframework.security.core.userdetails.UserDetails { // UserDetails uyguladığınızı varsayıyorum

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false) // Daha önceki hatayı çözdüğümüz alan
    private String fullName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private UserRole role; // Role enum'unuz olduğunu varsayıyorum

    // Bu kullanıcı tarafından oluşturulan tarihi şahsiyetler
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // <<< Bu satırı ekleyin
    private List<HistoricalFigure> createdFigures;

    // Constructors, getters, setters (diğer metodları da ekleyin, UserDetails implementasyonu için)

    public User() {
    }

    public User(Long id, String email, String password, String fullName, Instant createdAt, UserRole role, List<HistoricalFigure> createdFigures) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.createdAt = createdAt;
        this.role = role;
        this.createdFigures = createdFigures;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserRole getRole() {
        return role;
    }

    public List<HistoricalFigure> getCreatedFigures() {
        return createdFigures;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setCreatedFigures(List<HistoricalFigure> createdFigures) {
        this.createdFigures = createdFigures;
    }

    // UserDetails metodları (kısaltıldı)
    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email; // E-postayı kullanıcı adı olarak kullanıyoruz
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
