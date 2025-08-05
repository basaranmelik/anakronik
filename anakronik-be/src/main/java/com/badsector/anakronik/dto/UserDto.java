package com.badsector.anakronik.dto;

import com.badsector.anakronik.model.UserRole;

import java.time.Instant;

public record UserDto (Long id, String email, String fullName, UserRole role, Instant createdAt){}

