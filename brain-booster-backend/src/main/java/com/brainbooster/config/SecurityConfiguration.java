package com.brainbooster.config;

import com.brainbooster.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private static final String USER_BY_ID = "/users/*";
    private static final String FLASHCARD_BY_ID = "/flashcards/*";
    private static final String FLASHCARD_SET_BY_ID = "/flashcard-sets/*";
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // auth
                        .requestMatchers(HttpMethod.POST, "/auth/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // users - public
                        .requestMatchers(HttpMethod.GET, "/users/*/flashcard-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/nickname/*/flashcard-sets").permitAll()

                        // users - only admin
                        .requestMatchers(HttpMethod.GET, "/users").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/users").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, USER_BY_ID).hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, USER_BY_ID).hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, USER_BY_ID).hasAuthority(Role.ADMIN.name())

                        // users - authenticated
                        .requestMatchers(HttpMethod.PATCH, "/users/*/nickname").authenticated()

                        // flashcards - public
                        .requestMatchers(HttpMethod.GET, "/flashcards").permitAll()
                        .requestMatchers(HttpMethod.GET, FLASHCARD_BY_ID).permitAll()

                        // flashcards - authenticated
                        .requestMatchers(HttpMethod.POST, "/flashcards").authenticated()
                        .requestMatchers(HttpMethod.PATCH, FLASHCARD_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FLASHCARD_BY_ID).authenticated()

                        // flashcardSets - public endpoints
                        .requestMatchers(HttpMethod.GET, "/flashcard-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, FLASHCARD_SET_BY_ID).permitAll()
                        .requestMatchers(HttpMethod.GET, "/flashcard-sets/*/flashcards").permitAll()

                        // flashcardSets - authenticated
                        .requestMatchers(HttpMethod.POST, "/flashcard-sets").authenticated()
                        .requestMatchers(HttpMethod.PATCH, FLASHCARD_SET_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FLASHCARD_SET_BY_ID).authenticated()


                        .anyRequest().hasAuthority(Role.ADMIN.name())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}