package com.brainbooster.integration;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for repository slice tests.
 * It uses a lightweight Spring context (only JPA components) and
 * prevents Spring from replacing our Testcontainers PostgreSQL with an in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test") // forces loading of application-test.yaml
// IMPORTANT: tells Spring NOT to replace Postgres container with H2
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractRepositoryTest extends BaseTestEnvironment {

    /* Tests extending this class will automatically rollback transactions after each test,
     so no clear-database.sql script is needed here.
*/
}
