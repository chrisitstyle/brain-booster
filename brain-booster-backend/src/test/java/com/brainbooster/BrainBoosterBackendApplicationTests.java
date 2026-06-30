package com.brainbooster;

import com.brainbooster.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BrainBoosterBackendApplicationTests {

    @MockitoBean
    private JwtService jwtService;

    @Test
    void contextLoads() {
        assertThat(jwtService).isNotNull();
    }
}
