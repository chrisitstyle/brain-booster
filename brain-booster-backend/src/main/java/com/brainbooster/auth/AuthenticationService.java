package com.brainbooster.auth;

import com.brainbooster.config.JwtService;
import com.brainbooster.user.Role;
import com.brainbooster.user.UserAccountCreator;
import com.brainbooster.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String ACCOUNT_CREATED_MESSAGE = "Account has been created";

    private final UserRepository userRepository;
    private final UserAccountCreator userAccountCreator;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(@Valid RegisterRequest request) {
        userAccountCreator.create(
                request.getNickname(),
                request.getEmail(),
                request.getPassword(),
                Role.USER);

        return ACCOUNT_CREATED_MESSAGE;
    }

    public AuthenticationResponse authenticate(
            @Valid AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
