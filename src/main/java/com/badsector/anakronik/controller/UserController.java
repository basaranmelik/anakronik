package com.badsector.anakronik.controller;

import com.badsector.anakronik.controller.dto.UpdateUserRequest;
import com.badsector.anakronik.dto.UserDto;
import com.badsector.anakronik.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UserDto userDto = userService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        UserDto updatedUser = userService.updateCurrentUserProfile(authentication.getName(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        userService.deleteCurrentUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}