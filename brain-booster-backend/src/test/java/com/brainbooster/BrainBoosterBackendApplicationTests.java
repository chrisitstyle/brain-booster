package com.brainbooster;

import com.brainbooster.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

//@SpringBootTest
class BrainBoosterBackendApplicationTests {

    @MockitoBean
    private JwtService jwtService;

    @Test
    void contextLoads() {
    }


}
