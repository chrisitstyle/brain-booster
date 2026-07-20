package com.brainbooster.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // loading application-test.yaml
@AutoConfigureMockMvc // enables MockMvc within the full SpringBootTest context
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class AbstractIntegrationTest {

    // definition of the PostgreSQL container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    /* Singleton Container Pattern
     The static block will execute only ONCE when the classloader loads this class
     Ryuk (Testcontainers sidecar) will automatically shut down the container when the JVM stops
     */
    static {
        postgres.start();
    }
}