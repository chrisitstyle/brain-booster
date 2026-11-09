package com.brainbooster.spacedrepetition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a shared UTC clock for spaced repetition services.
 *
 * <p>Using an injectable clock allows queue availability and daily
 * review limits to be tested without depending on the system time.
 */
@Configuration
public class ReviewClockConfig {

    @Bean
    public Clock reviewClock() {
        return Clock.systemUTC();
    }
}
