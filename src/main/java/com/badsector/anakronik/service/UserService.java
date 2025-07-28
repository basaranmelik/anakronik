package com.badsector.anakronik.service;

import com.badsector.anakronik.dto.UpdateUserRequest;
import com.badsector.anakronik.dto.UserDto;
import com.badsector.anakronik.mapper.UserMapper;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.repository.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final HistoricalFigureRepository historicalFigureRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       HistoricalFigureRepository historicalFigureRepository,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.historicalFigureRepository = historicalFigureRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile(String currentUserEmail) {
        User user = findByEmail(currentUserEmail);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateCurrentUserProfile(String currentUserEmail, UpdateUserRequest request) {
        User userToUpdate = findByEmail(currentUserEmail);
        userToUpdate.setFullName(request.fullName());
        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteCurrentUser(String currentUserEmail) {
        User userToDelete = findByEmail(currentUserEmail);

        refreshTokenRepository.deleteByUser(userToDelete);
        historicalFigureRepository.deleteAll(historicalFigureRepository.findByCreatedBy(userToDelete));
        userRepository.delete(userToDelete);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}