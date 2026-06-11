package com.brainbooster.config;

import com.brainbooster.user.Role;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String FLASHCARD_STARRED = "/flashcards/*/starred";
    private static final String FLASHCARD_SET_BY_ID = "/flashcard-sets/*";
    private static final String FOLDER_BY_ID = "/folders/*";
    private static final String FOLDER_SET_BY_ID = "/folders/*/sets/*";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // swagger / openapi
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()

                        // auth
                        .requestMatchers(HttpMethod.POST, "/auth/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // users - public
                        .requestMatchers(HttpMethod.GET, "/users/*/flashcard-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/nickname/*/flashcard-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*/folders").permitAll()

                        // users - only admin
                        .requestMatchers(HttpMethod.GET, "/users").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, USER_BY_ID).hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/users").hasAuthority(Role.ADMIN.name())
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
                        .requestMatchers(HttpMethod.POST, FLASHCARD_STARRED).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FLASHCARD_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FLASHCARD_STARRED).authenticated()

                        // flashcardSets - public endpoints
                        .requestMatchers(HttpMethod.GET, "/flashcard-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, FLASHCARD_SET_BY_ID).permitAll()
                        .requestMatchers(HttpMethod.GET, "/flashcard-sets/*/flashcards").permitAll()

                        // flashcardSets - authenticated
                        .requestMatchers(HttpMethod.POST, "/flashcard-sets").authenticated()
                        .requestMatchers(HttpMethod.PATCH, FLASHCARD_SET_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FLASHCARD_SET_BY_ID).authenticated()

                        // folders - public
                        .requestMatchers(HttpMethod.GET, "/folders").permitAll()
                        .requestMatchers(HttpMethod.GET, FOLDER_BY_ID).permitAll()

                        // folders - authenticated
                        .requestMatchers(HttpMethod.POST, "/folders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/folders/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, FOLDER_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FOLDER_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.POST, FOLDER_SET_BY_ID).authenticated()
                        .requestMatchers(HttpMethod.DELETE, FOLDER_SET_BY_ID).authenticated()

                        // game results
                        .requestMatchers("/game-results/**").authenticated()
                        // game attempts
                        .requestMatchers("/game-attempts/**").authenticated()


                        .anyRequest().hasAuthority(Role.ADMIN.name())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN)
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}