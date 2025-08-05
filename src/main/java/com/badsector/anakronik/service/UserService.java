package com.badsector.anakronik.service;

import com.badsector.anakronik.controller.dto.UpdateUserRequest;
import com.badsector.anakronik.dto.UserDto;
import com.badsector.anakronik.exception.ResourceNotFoundException;
import com.badsector.anakronik.mapper.UserMapper;
import com.badsector.anakronik.model.HistoricalFigure;
import com.badsector.anakronik.model.User;
import com.badsector.anakronik.model.VerificationToken;
import com.badsector.anakronik.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final HistoricalFigureRepository historicalFigureRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       HistoricalFigureRepository historicalFigureRepository,
                       RefreshTokenRepository refreshTokenRepository, ChatMessageRepository chatMessageRepository, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.historicalFigureRepository = historicalFigureRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.verificationTokenRepository = verificationTokenRepository;
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
        deleteUserAndAssociatedData(userToDelete);
    }


    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Transactional
    public UserDto updateUserAsAdmin(Long userId, UpdateUserRequest request) {
        User userToUpdate = findById(userId);
        userToUpdate.setFullName(request.fullName());
        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUserAsAdmin(Long userId) {
        User userToDelete = findById(userId);
        deleteUserAndAssociatedData(userToDelete);
    }

    @Transactional
    private void deleteUserAndAssociatedData(User user) {
        List<HistoricalFigure> figuresToDelete = historicalFigureRepository.findByCreatedBy(user);
        for (HistoricalFigure figure : figuresToDelete) {
            chatMessageRepository.deleteByHistoricalFigure(figure);
        }
        verificationTokenRepository.deleteByUser(user);
        historicalFigureRepository.deleteAll(figuresToDelete);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}