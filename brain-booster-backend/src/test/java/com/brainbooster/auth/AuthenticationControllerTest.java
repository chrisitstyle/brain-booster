package com.brainbooster.auth;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.ErrorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_ShouldReturnCreatedAndMessage_WhenSuccess() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("register_user", "registeruser@test.com",
                "password");
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn("Account has been created");

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
        RegisterRequest request = new RegisterRequest("user", "takenemail@test.com",
                "password");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("User with this email already exists"));

        // when
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());

        ErrorDTO errorResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );
        assertThat(errorResponse.message()).isEqualTo("User with this email already exists");
        assertThat(errorResponse.status()).isEqualTo("UNPROCESSABLE_CONTENT");
    }

    @Test
    void authenticate_ShouldReturnOkAndToken_WhenSuccess() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest("johndoe@test.com",
                "password");
        AuthenticationResponse response = new AuthenticationResponse("valid_jwt_token");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        // when
        MvcResult result = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

        AuthenticationResponse responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );
        assertThat(responseBody.getToken()).isEqualTo("valid_jwt_token");
    }

    @Test
    void authenticate_ShouldReturnUnauthorized_WhenBadCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest("badcredentials@test.com", "wrongpass");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // when
        MvcResult result = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getResponse().getContentAsString()).isEqualTo("Invalid username or password");
    }
}
