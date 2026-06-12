package com.brainbooster.profile;

import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.NicknameAlreadyExistsException;
import com.brainbooster.profile.dto.UserEmailUpdateDTO;
import com.brainbooster.profile.dto.UserEmailUpdateResponseDTO;
import com.brainbooster.profile.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.User;
import com.brainbooster.user.UserDTOMapper;
import com.brainbooster.user.UserRepository;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProfileSettingsService {

    private final UserRepository userRepository;
    private final UserDTOMapper userDTOMapper;
    private final JwtService jwtService;

    @Transactional
    public UserDTO updateNickname(UserNicknameUpdateDTO request) {
        User user = getAuthenticatedUser();

        String newNickname = request.newNickname().trim();

        if (newNickname.equals(user.getNickname())) {
            return userDTOMapper.apply(user);
        }

        if (userRepository.existsByNickname(newNickname)) {
            throw new NicknameAlreadyExistsException(
                    "Nickname is already taken"
            );
        }

        user.setNickname(newNickname);

        return userDTOMapper.apply(user);
    }

    @Transactional
    public UserEmailUpdateResponseDTO updateEmail(
            UserEmailUpdateDTO request
    ) {
        User user = getAuthenticatedUser();

        String newEmail = request.newEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return new UserEmailUpdateResponseDTO(
                    user.getEmail(),
                    jwtService.generateToken(user)
            );
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException(
                    "Email is already taken"
            );
        }

        user.setEmail(newEmail);

        return new UserEmailUpdateResponseDTO(
                user.getEmail(),
                jwtService.generateToken(user)
        );
    }

    private User getAuthenticatedUser() {
        User principal = SecurityUtils.getAuthenticatedUser();

        return userRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new NoSuchElementException("Authenticated user not found")
                );
    }
}