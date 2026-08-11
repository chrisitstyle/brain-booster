package com.brainbooster.user;

import com.brainbooster.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserAccountCreator {

    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "User with this email already exists";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(
            String nickname,
            String email,
            String rawPassword,
            Role role
    ) {
        verifyEmailIsAvailable(email);

        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    private void verifyEmailIsAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS_MESSAGE);
        }
    }
}
