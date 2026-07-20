package com.brainbooster.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base environment class for all tests requiring a database.
 * It implements the Singleton Container Pattern to ensure only one instance
 * of PostgreSQL is started for the entire test suite, significantly reducing test execution time.
 */
public abstract class BaseTestEnvironment {

    // definition of the PostgreSQL container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    /*
     * Singleton Container Pattern
     * The static block will execute only ONCE when the classloader loads this class.
     * Ryuk (Testcontainers sidecar) will automatically shut down the container when the JVM stops.
     */
    static {
        postgres.start();
    }

}