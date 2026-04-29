package com.library.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.User;
import com.library.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @SuppressWarnings("null")
    @Transactional
    public void recordSuccessfulLogin(String userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return;
        }
        User u = opt.get();
        u.setLastLoginAt(Instant.now());
        userRepository.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
