package com.brainbooster.integration.flashcardset;

import com.brainbooster.config.JwtService;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/insert-it-test-users.sql")
class FlashcardSetControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;

    // -- CREATE TESTS --

    @Test
    @DisplayName("POST /flashcard-sets - Should create flashcard set and return 201 Created")
    void addFlashcardSet_ShouldReturn201() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String userToken = jwtService.generateToken(userFromDB);

        FlashcardSetCreationDTO creationDTO = new FlashcardSetCreationDTO(
                "My New Set","Set Description");

        // when, then
        mockMvc.perform(post("/flashcard-sets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setName").value("My New Set"))
                .andExpect(jsonPath("$.description").value("Set Description"));
    }

    @Test
    @DisplayName("POST /flashcard-sets - Should return 400 Bad Request when request body is invalid")
    void addFlashcardSet_ShouldReturn400_WhenInvalidData() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String userToken = jwtService.generateToken(userFromDB);

        // blank name should trigger @Valid failure
        FlashcardSetCreationDTO invalidDto = new FlashcardSetCreationDTO(
                "",
                "Description");

        // when, then
        mockMvc.perform(post("/flashcard-sets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /flashcard-sets - Should return 401 Unauthorized when missing token")
    void addFlashcardSet_ShouldReturn401_WhenNoToken() throws Exception {
        // given
        FlashcardSetCreationDTO creationDTO = new FlashcardSetCreationDTO(
                "Name",
                "Desc");

        // when, then
        mockMvc.perform(post("/flashcard-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isUnauthorized());
    }

    // -- READ TESTS --

    @Test
    @DisplayName("GET /flashcard-sets - Should return list of all flashcard sets (200 OK)")
    void getAllFlashcardSets_ShouldReturn200() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        flashcardSetRepository.save(TestEntities
                .flashcardSetBuilder().setId(null)
                .user(userFromDB).setName("Set 1").build());
        flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder().setId(null)
                        .user(userFromDB).setName("Set 2").build());

        // when, then
        mockMvc.perform(get("/flashcard-sets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].setName").value("Set 1"));
    }

    @Test
    @DisplayName("GET /flashcard-sets/{setId} - Should fetch single flashcard set (200 OK)")
    void getFlashcardSetById_ShouldReturn200() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder().setId(null)
                        .user(userFromDB).setName("Single Set").build());

        // when, then
        mockMvc.perform(get("/flashcard-sets/" + savedSet.getSetId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setName").value("Single Set"));
    }

    @Test
    @DisplayName("GET /flashcard-sets/{setId} - Should return 404 Not Found when set does not exist")
    void getFlashcardSetById_ShouldReturn404_WhenNotFound() throws Exception {
        // when, then
        mockMvc.perform(get("/flashcard-sets/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /flashcard-sets/{setId}/flashcards - Should return all flashcards inside the set (200 OK)")
    void getAllFlashcardsInSet_ShouldReturn200() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB).build());

        Flashcard flashcard = TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Term1").build();

        flashcardRepository.save(flashcard);

        // when, then
        mockMvc.perform(get("/flashcard-sets/" + savedSet.getSetId() + "/flashcards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].term").value("Term1"));
    }

    @Test
    @DisplayName("GET /flashcard-sets/{setId}/flashcards - Should return 404 Not Found when set does not exist")
    void getAllFlashcardsInSet_ShouldReturn404_WhenSetNotFound() throws Exception {
        // when, then
        mockMvc.perform(get("/flashcard-sets/99999/flashcards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("PATCH /flashcard-sets/{setId} - Should update flashcard set and return 200 OK")
    void updateFlashcardSet_ShouldReturn200() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String token = jwtService.generateToken(userFromDB);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB)
                        .setName("Old Name").build());

        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "New Name",
                "New Desc");

        // when, then
        mockMvc.perform(patch("/flashcard-sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setName").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Desc"));

        FlashcardSet updatedSet = flashcardSetRepository.findById(savedSet.getSetId()).orElseThrow();
        assertThat(updatedSet.getSetName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("PATCH /flashcard-sets/{setId} - Should return 400 Bad Request when update data is invalid")
    void updateFlashcardSet_ShouldReturn400_WhenInvalidData() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String userToken = jwtService.generateToken(userFromDB);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB).build());

        FlashcardSetUpdateDTO invalidDto = new FlashcardSetUpdateDTO(
                "",
                "Valid desc"); // blank name

        // when, then
        mockMvc.perform(patch("/flashcard-sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /flashcard-sets/{setId} - Should return 401 Unauthorized when missing token")
    void updateFlashcardSet_ShouldReturn401_WhenNoToken() throws Exception {
        // given
        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "New Name",
                "New Desc");

        // when, then
        mockMvc.perform(patch("/flashcard-sets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /flashcard-sets/{setId} - Should return 403 Forbidden when updating someone else's set")
    void updateFlashcardSet_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        String anotherUserToken = jwtService.generateToken(anotherUser);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "Hacked Name",
                "Hacked Desc");

        // when, then
        mockMvc.perform(patch("/flashcard-sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /flashcard-sets/{setId} - Should return 404 Not Found when updating non-existent set")
    void updateFlashcardSet_ShouldReturn404_WhenNotFound() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String token = jwtService.generateToken(userFromDB);
        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "New Name",
                "New Desc");

        // when, then
        mockMvc.perform(patch("/flashcard-sets/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    // -- DELETE TESTS --

    @Test
    @DisplayName("DELETE /flashcard-sets/{setId} - Should delete flashcard set and return 204 No Content")
    void deleteFlashcardSet_ShouldReturn204() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String token = jwtService.generateToken(userFromDB);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB).build());

        // when, then
        mockMvc.perform(delete("/flashcard-sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(flashcardSetRepository.findById(savedSet.getSetId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /flashcard-sets/{setId} - Should return 401 Unauthorized when missing token")
    void deleteFlashcardSet_ShouldReturn401_WhenNoToken() throws Exception {
        // when, then
        mockMvc.perform(delete("/flashcard-sets/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /flashcard-sets/{setId} - Should return 403 Forbidden when deleting someone else's set")
    void deleteFlashcardSet_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        String anotherUserToken = jwtService.generateToken(anotherUser);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        // when, then
        mockMvc.perform(delete("/flashcard-sets/" + savedSet.getSetId())
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /flashcard-sets/{setId} - Should return 404 Not Found when deleting non-existent set")
    void deleteFlashcardSet_ShouldReturn404_WhenNotFound() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String token = jwtService.generateToken(userFromDB);

        // when, then
        mockMvc.perform(delete("/flashcard-sets/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
