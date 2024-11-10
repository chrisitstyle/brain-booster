package com.brainbooster;

import com.brainbooster.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class BrainBoosterBackendApplicationTests {

	@MockBean
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}


}
