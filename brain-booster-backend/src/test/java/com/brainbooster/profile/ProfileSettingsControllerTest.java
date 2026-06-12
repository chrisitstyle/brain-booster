package com.brainbooster.profile;

import com.brainbooster.config.JwtAuthenticationFilter;
import com.brainbooster.config.JwtService;
import com.brainbooster.exception.EmailAlreadyExistsException;
import com.brainbooster.exception.NicknameAlreadyExistsException;
import com.brainbooster.profile.dto.UserEmailUpdateDTO;
import com.brainbooster.profile.dto.UserEmailUpdateResponseDTO;
import com.brainbooster.profile.dto.UserNicknameUpdateDTO;
import com.brainbooster.user.dto.UserDTO;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileSettingsService profileSettingsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = TestEntities.createUserDTO();
    }

    @Test
    void updateNickname_ShouldReturnUpdatedUserDTO_WhenRequestIsValid()
            throws Exception {
        // given
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("newNickname");

        UserDTO updatedUserDTO = new UserDTO(
                userDTO.userId(),
                "newNickname",
                userDTO.email(),
                userDTO.role(),
                userDTO.createdAt()
        );

        given(profileSettingsService.updateNickname(
                any(UserNicknameUpdateDTO.class)
        )).willReturn(updatedUserDTO);

        // when, then
        mockMvc.perform(
                        patch("/profile/settings/nickname")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId")
                        .value(updatedUserDTO.userId()))
                .andExpect(jsonPath("$.nickname")
                        .value("newNickname"))
                .andExpect(jsonPath("$.email")
                        .value(updatedUserDTO.email()))
                .andExpect(jsonPath("$.role")
                        .value(updatedUserDTO.role().name()));

        verify(profileSettingsService)
                .updateNickname(any(UserNicknameUpdateDTO.class));
    }

    @Test
    void updateEmail_ShouldReturnEmailAndToken_WhenRequestIsValid()
            throws Exception {
        // given
        UserEmailUpdateDTO request =
                new UserEmailUpdateDTO("new@example.com");

        UserEmailUpdateResponseDTO response =
                new UserEmailUpdateResponseDTO(
                        "new@example.com",
                        "new-jwt-token"
                );

        given(profileSettingsService.updateEmail(
                any(UserEmailUpdateDTO.class)
        )).willReturn(response);

        // when, then
        mockMvc.perform(
                        patch("/profile/settings/email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("new@example.com"))
                .andExpect(jsonPath("$.token")
                        .value("new-jwt-token"));

        verify(profileSettingsService)
                .updateEmail(any(UserEmailUpdateDTO.class));
    }

    @Test
    void updateNickname_ShouldReturnBadRequest_WhenNicknameIsBlank()
            throws Exception {
        // when, then
        mockMvc.perform(
                        patch("/profile/settings/nickname")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newNickname": " "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value("BAD_REQUEST"));

        verifyNoInteractions(profileSettingsService);
    }

    @Test
    void updateEmail_ShouldReturnBadRequest_WhenEmailIsInvalid()
            throws Exception {
        // when, then
        mockMvc.perform(
                        patch("/profile/settings/email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "newEmail": "invalid-email"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value("BAD_REQUEST"));

        verifyNoInteractions(profileSettingsService);
    }

    @Test
    void updateNickname_ShouldReturnUnprocessableContent_WhenNicknameIsTaken()
            throws Exception {
        // given
        UserNicknameUpdateDTO request =
                new UserNicknameUpdateDTO("takenNickname");

        given(profileSettingsService.updateNickname(
                any(UserNicknameUpdateDTO.class)
        )).willThrow(
                new NicknameAlreadyExistsException(
                        "Nickname is already taken"
                )
        );

        // when, then
        mockMvc.perform(
                        patch("/profile/settings/nickname")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Nickname is already taken"))
                .andExpect(jsonPath("$.status")
                        .value("CONFLICT"));
    }

    @Test
    void updateEmail_ShouldReturnUnprocessableContent_WhenEmailIsTaken()
            throws Exception {
        // given
        UserEmailUpdateDTO request =
                new UserEmailUpdateDTO("taken@example.com");

        given(profileSettingsService.updateEmail(
                any(UserEmailUpdateDTO.class)
        )).willThrow(
                new EmailAlreadyExistsException(
                        "Email is already taken"
                )
        );

        // when, then
        mockMvc.perform(
                        patch("/profile/settings/email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message")
                        .value("Email is already taken"))
                .andExpect(jsonPath("$.status")
                        .value("UNPROCESSABLE_CONTENT"));
    }
}
