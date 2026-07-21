package com.brainbooster;

import com.brainbooster.config.JwtService;
import com.brainbooster.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

class BrainBoosterBackendApplicationTests extends AbstractIntegrationTest {

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
