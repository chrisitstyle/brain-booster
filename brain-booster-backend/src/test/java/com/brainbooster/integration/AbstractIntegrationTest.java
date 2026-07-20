package com.brainbooster.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Base class for E2E and API integration tests.
 * It loads the full Spring application context and ensures the database
 * is truncated after each test method runs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // loading of application-test.yaml
@AutoConfigureMockMvc // enables MockMvc within the full SpringBootTest context
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public abstract class AbstractIntegrationTest extends BaseTestEnvironment {

    /*
     No container definition needed here because it's inherited from BaseTestEnvironment
     */

}