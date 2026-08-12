package com.brainbooster.auth;

import com.brainbooster.config.JwtService;
import com.brainbooster.security.UserPrincipal;
import com.brainbooster.user.Role;
import com.brainbooster.user.UserAccountCreator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String ACCOUNT_CREATED_MESSAGE = "Account has been created";

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationServiceException(
                    "Invalid authenticated principal");
        }

        String jwtToken = jwtService.generateToken(principal);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
