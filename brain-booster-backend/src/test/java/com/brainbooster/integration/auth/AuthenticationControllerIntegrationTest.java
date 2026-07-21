package com.brainbooster.integration.auth;

import com.brainbooster.auth.AuthenticationRequest;
import com.brainbooster.auth.AuthenticationResponse;
import com.brainbooster.auth.RegisterRequest;
import com.brainbooster.exception.ErrorDTO;
import com.brainbooster.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


class AuthenticationControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreatedAndMessage_WhenSuccess() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("integration_user", "integration@test.com", "password123");

        // when
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Account has been created");
    }

    @Test
    void register_ShouldReturnUnprocessableContent_WhenEmailExists() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("duplicate_user", "taken@test.com", "password123");

        // save the user in the real database for the first time
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // when - trying to register exactly the same account again
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();


        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());

        ErrorDTO errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);

        assertThat(errorResponse.message()).contains("already exists");
        assertThat(errorResponse.status()).isEqualTo("UNPROCESSABLE_CONTENT");
    }

    @Test
    void authenticate_ShouldReturnOkAndToken_WhenSuccess() throws Exception {
        // given
        RegisterRequest registerRequest = new RegisterRequest("john_login", "johnlogin@test.com", "securepass");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        AuthenticationRequest authRequest = new AuthenticationRequest("johnlogin@test.com", "securepass");

        // when
        MvcResult result = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        AuthenticationResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthenticationResponse.class
        );

        assertThat(responseBody.getToken()).isNotBlank();
        assertThat(responseBody.getToken().split("\\.")).hasSize(3);
    }

    @Test
    void authenticate_ShouldReturnUnauthorized_WhenBadCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest("nonexistent@test.com", "wrongpass");

        // when
        MvcResult result = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
