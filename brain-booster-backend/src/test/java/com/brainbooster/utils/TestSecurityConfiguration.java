package com.brainbooster.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Provides a simplified Spring Security configuration for controller tests.
 * <p>
 * This configuration enables Spring Security integration in the test context
 * while allowing access to every endpoint without requiring a valid JWT token.
 * It is intended primarily for {@code @WebMvcTest} tests that verify controller
 * behavior independently of the application's production security configuration.
 * </p>
 * <p>
 * CSRF protection is disabled to simplify testing of state-changing HTTP
 * requests such as POST, PATCH, and DELETE.
 * </p>
 */
@TestConfiguration(proxyBeanMethods = false)
@EnableWebSecurity
public class TestSecurityConfiguration {

    /**
     * Creates the security filter chain used during controller tests.
     * <p>
     * The configured filter chain:
     * </p>
     * <ul>
     *     <li>disables CSRF protection,</li>
     *     <li>allows access to every request,</li>
     *     <li>keeps Spring Security filters active so that test authentication
     *     can be propagated to {@code @AuthenticationPrincipal} parameters.</li>
     * </ul>
     *
     * @param http the Spring Security HTTP configuration builder
     * @return the configured test security filter chain
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                )
                .build();
    }
}
