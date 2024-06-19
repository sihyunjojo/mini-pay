package org.c4marathon.assignment.service;


import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.generator.NicknameGenerator;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        String name = user.getName();
        String email = user.getEmail();
        String nickname = nicknameGenerator.generateNickname(name,email);

        user.setNickname(nickname);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        Optional<User> byId = userRepository.findById(id);
        return byId;
    }
}