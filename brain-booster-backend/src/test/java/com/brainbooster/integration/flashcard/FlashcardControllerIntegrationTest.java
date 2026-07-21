package com.brainbooster.integration.flashcard;

import com.brainbooster.config.JwtService;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
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
class FlashcardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
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
    @DisplayName("POST /flashcards - Should create flashcard and return 201 Created")
    void addFlashcardShouldReturnFlashcardDTO() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet saveSetToDB = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(saveSetToDB);

        FlashcardCreationDTO flashcardCreationDTO = new FlashcardCreationDTO(
                savedSet.getSetId(),
                "test-it-term",
                "test-it-definition"
        );

        // when, then
        mockMvc.perform(post("/flashcards")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("test-it-term"))
                .andExpect(jsonPath("$.definition").value("test-it-definition"));
    }

    @Test
    @DisplayName("POST /flashcards - Should return 400 Bad Request when request body is invalid")
    void addFlashcard_ShouldReturn400_WhenInvalidData() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardCreationDTO invalidDto = new FlashcardCreationDTO(
                1L,
                "",
                "some definition");

        // when, then
        mockMvc.perform(post("/flashcards")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /flashcards - Should return 401 Unauthorized when missing token")
    void addFlashcard_ShouldReturn401_WhenNoToken() throws Exception {
        // given
        FlashcardCreationDTO flashcardCreationDTO = new FlashcardCreationDTO(
                1L,
                "term",
                "def");

        // when, then
        mockMvc.perform(post("/flashcards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardCreationDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /flashcards - Should return 403 Forbidden when trying to add card to another user's set")
    void addFlashcard_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow();
        User anotherRegularUser = userRepository.findById(3L).orElseThrow();
        String tokenForAnotherUser = jwtService.generateToken(anotherRegularUser);

        FlashcardSet regularUserSet = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(regularUser)
                .build();
        FlashcardSet savedRegularUserSet = flashcardSetRepository.save(regularUserSet);

        FlashcardCreationDTO flashcardCreationDTO = new FlashcardCreationDTO(
                savedRegularUserSet.getSetId(),
                "term",
                "definition"
        );

        // when, then
        mockMvc.perform(post("/flashcards")
                        .header("Authorization", "Bearer " + tokenForAnotherUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardCreationDTO)))
                .andExpect(status().isForbidden());
    }

    // -- READ TESTS --

    @Test
    @DisplayName("GET /flashcards - Should return list of all flashcards (200 OK)")
    void getAllFlashcards_ShouldReturnFlashcardList() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet setToDB = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcardToDB = TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Test Term").build();

        flashcardRepository.save(flashcardToDB);

        // when, then
        mockMvc.perform(get("/flashcards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].term").value("Test Term"));
    }

    @Test
    @DisplayName("GET /flashcards/{id} - Should fetch single flashcard by ID (200 OK)")
    void getFlashcardById_ShouldReturnFlashcard() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet setToDB = TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(userFromDB).build();

        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcard = TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Single Term")
                .definition("Single Def").build();

        Flashcard savedCard = flashcardRepository.save(flashcard);

        // when, then
        mockMvc.perform(get("/flashcards/" + savedCard.getFlashcardId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Single Term"))
                .andExpect(jsonPath("$.definition").value("Single Def"));
    }

    @Test
    @DisplayName("GET /flashcards/{id} - Should return 404 Not Found when flashcard does not exist")
    void getFlashcardById_ShouldReturn404_WhenNotFound() throws Exception {
        // when, then
        mockMvc.perform(get("/flashcards/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should update flashcard term and definition (200 OK)")
    void updateFlashcard_ShouldModifyCardInDB() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet setToDB = TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcard = TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Old Term")
                .definition("Old Def")
                .build();
        Flashcard savedCard = flashcardRepository.save(flashcard);

        FlashcardUpdateDTO flashcardUpdateDTO = new FlashcardUpdateDTO("New Term", "New Def");

        // when, then
        mockMvc.perform(patch("/flashcards/" + savedCard.getFlashcardId())
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("New Term"))
                .andExpect(jsonPath("$.definition").value("New Def"));

        Flashcard updatedCard = flashcardRepository.findById(savedCard.getFlashcardId()).orElseThrow();
        assertThat(updatedCard.getTerm()).isEqualTo("New Term");
    }

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should return 400 Bad Request when update data is invalid")
    void updateFlashcard_ShouldReturn400_WhenInvalidData() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet setToDB = TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcard = TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet).build();

        Flashcard savedCard = flashcardRepository.save(flashcard);

        FlashcardUpdateDTO invalidDto = new FlashcardUpdateDTO("", "Valid def");

        // when, then
        mockMvc.perform(patch("/flashcards/" + savedCard.getFlashcardId())
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should return 401 Unauthorized when missing token")
    void updateFlashcard_ShouldReturn401_WhenNoToken() throws Exception {
        // given
        FlashcardUpdateDTO updateDto = new FlashcardUpdateDTO("term", "def");

        // when, then
        mockMvc.perform(patch("/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should return 403 Forbidden when updating someone else's flashcard")
    void updateFlashcard_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow();
        User anotherRegularUser = userRepository.findById(3L).orElseThrow();
        String tokenForAnotherUser = jwtService.generateToken(anotherRegularUser);

        FlashcardSet regularUserSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(regularUser)
                        .build());

        Flashcard regularUserCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(regularUserSet).build());

        FlashcardUpdateDTO flashcardUpdateDTO = new FlashcardUpdateDTO(
                "Hacked Term",
                "Hacked Def");

        // when, then
        mockMvc.perform(patch("/flashcards/" + regularUserCard.getFlashcardId())
                        .header("Authorization", "Bearer " + tokenForAnotherUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should return 404 Not Found when updating non-existent flashcard")
    void updateFlashcard_ShouldReturn404_WhenNotFound() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);
        FlashcardUpdateDTO flashcardUpdateDTO = new FlashcardUpdateDTO(
                "New Term",
                "New Def");

        // when, then
        mockMvc.perform(patch("/flashcards/99999")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    // -- DELETE TESTS --

    @Test
    @DisplayName("DELETE /flashcards/{id} - Should remove flashcard and return 204 No Content")
    void deleteFlashcardById_ShouldRemoveFromDB() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB).build());

        Flashcard flashcard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(set).build());

        // when, then
        mockMvc.perform(delete("/flashcards/" + flashcard.getFlashcardId())
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(flashcardRepository.findById(flashcard.getFlashcardId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /flashcards/{id} - Should return 401 Unauthorized when missing token")
    void deleteFlashcardById_ShouldReturn401_WhenNoToken() throws Exception {
        // when, then
        mockMvc.perform(delete("/flashcards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /flashcards/{id} - Should return 403 Forbidden when deleting someone else's flashcard")
    void deleteFlashcardById_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User hacker = userRepository.findById(3L).orElseThrow();
        String hackerToken = jwtService.generateToken(hacker);

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner)
                        .build());

        Flashcard flashcard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(set)
                        .build());

        // when, then
        mockMvc.perform(delete("/flashcards/" + flashcard.getFlashcardId())
                        .header("Authorization", "Bearer " + hackerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /flashcards/{id} - Should return 404 Not Found when flashcard does not exist")
    void deleteFlashcardById_ShouldReturn404_WhenNotFound() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        // when, then
        mockMvc.perform(delete("/flashcards/99999")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -- STARRED TESTS --

    @Test
    @DisplayName("POST /flashcards/{id}/starred - Should star a flashcard (200 OK)")
    void starFlashcard_ShouldMarkAsStarred() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB).build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(set)
                        .build());

        // when, then
        mockMvc.perform(post("/flashcards/" + savedCard.getFlashcardId() + "/starred")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starred").value(true));
    }

    @Test
    @DisplayName("DELETE /flashcards/{id}/starred - Should unstar a flashcard (200 OK)")
    void unstarFlashcard_ShouldRemoveStar() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB)
                        .build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(set).build());

        mockMvc.perform(post("/flashcards/" + savedCard.getFlashcardId() + "/starred")
                        .header("Authorization", "Bearer " + realUserToken))
                .andExpect(status().isOk());

        // when, then (unstar)
        mockMvc.perform(delete("/flashcards/" + savedCard.getFlashcardId() + "/starred")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starred").value(false));
    }

    @Test
    @DisplayName("POST /flashcards/{id}/starred - Should return 401 Unauthorized when no token")
    void starFlashcard_ShouldReturn401_WhenNoToken() throws Exception {
        // when, then
        mockMvc.perform(post("/flashcards/1/starred")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /flashcards/{id}/starred - Should return 404 Not Found for non-existent flashcard")
    void starFlashcard_ShouldReturn404_WhenNotFound() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        // when, then
        mockMvc.perform(post("/flashcards/99999/starred")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}