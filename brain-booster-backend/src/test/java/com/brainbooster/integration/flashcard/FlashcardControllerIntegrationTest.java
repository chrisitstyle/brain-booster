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
    @DisplayName("POST /flashcards - Should create flashcard for dedicated user's set")
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
                savedSet.getSetId(), // setId from saved set in DB
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
    @DisplayName("POST /flashcards - Should return 403 Forbidden when trying to add card to another user's set")
    void addFlashcard_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow();
        User anotherRegularUser = userRepository.findById(3L).orElseThrow();
        String tokenForRegularUser = jwtService.generateToken(anotherRegularUser);

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

        // when, then - another regular user tries to add flashcard to regular user's set
        mockMvc.perform(post("/flashcards")
                        .header("Authorization", "Bearer " + tokenForRegularUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardCreationDTO)))
                .andExpect(status().isForbidden());
    }

    // -- READ TESTS --
    @Test
    @DisplayName("GET /flashcards - Should return list of all flashcards")
    void getAllFlashcards_ShouldReturnFlashcardList() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet setToDB = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();
        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcardToDB = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Test Term")
                .build();

        flashcardRepository.save(flashcardToDB);

        // when, then
        mockMvc.perform(get("/flashcards")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].term").value("Test Term"));
    }

    @Test
    @DisplayName("GET /flashcards/{id} - Should fetch single flashcard by ID")
    void getFlashcardById_ShouldReturnFlashcard() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        FlashcardSet setToDB = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();

        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Single Term")
                .definition("Single Def")
                .build();
        Flashcard savedCard = flashcardRepository.save(flashcard);

        // when, then
        mockMvc.perform(get("/flashcards/" + savedCard.getFlashcardId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Single Term"))
                .andExpect(jsonPath("$.definition").value("Single Def"));
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should update flashcard term and definition")
    void updateFlashcard_ShouldModifyCardInDB() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet setToDB = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(userFromDB)
                .build();

        FlashcardSet savedSet = flashcardSetRepository.save(setToDB);

        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Old Term")
                .definition("Old Def")
                .build();

        Flashcard savedCard = flashcardRepository.save(flashcard);
        FlashcardUpdateDTO flashcardUpdateDTO = new FlashcardUpdateDTO(
                "New Term",
                "New Def");

        // when, then
        mockMvc.perform(patch("/flashcards/" + savedCard.getFlashcardId())
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("New Term"))
                .andExpect(jsonPath("$.definition").value("New Def"));

        // verify in db
        Flashcard updatedCard = flashcardRepository.findById(savedCard.getFlashcardId()).orElseThrow();
        assertThat(updatedCard.getTerm()).isEqualTo("New Term");
    }

    @Test
    @DisplayName("PATCH /flashcards/{id} - Should return 403 when updating someone else's flashcard")
    void updateFlashcard_ShouldReturn403_WhenNotOwner() throws Exception {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow();
        User anotherRegularUser = userRepository.findById(3L).orElseThrow();
        String tokenForAnotherRegularUser = jwtService.generateToken(anotherRegularUser);

        FlashcardSet regularUserSet = flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(regularUser)
                        .build());

        Flashcard regularUserCard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(regularUserSet)
                .build();

        Flashcard savedRegularUserCard = flashcardRepository.save(regularUserCard);

        FlashcardUpdateDTO flashcardUpdateDTO = new FlashcardUpdateDTO("Hacked Term",
                "Hacked Def");
        // when, then
        mockMvc.perform(patch("/flashcards/" + savedRegularUserCard.getFlashcardId())
                        .header("Authorization", "Bearer " + tokenForAnotherRegularUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashcardUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    // -- DELETE TESTS --

    @Test
    @DisplayName("DELETE /flashcards/{id} - Should remove flashcard and return 204")
    void deleteFlashcardById_ShouldRemoveFromDB() throws Exception {
        // given
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);

        FlashcardSet set = flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB)
                        .build());

        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(set)
                .build();

        Flashcard savedCard = flashcardRepository.save(flashcard);

        // when, then
        mockMvc.perform(delete("/flashcards/" + savedCard.getFlashcardId())
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(flashcardRepository.findById(savedCard.getFlashcardId())).isEmpty();
    }

    // -- STARRED TESTS --

    @Test
    @DisplayName("POST /flashcards/{id}/starred - Should star a flashcard")
    void starFlashcard_ShouldMarkAsStarred() throws Exception {
        User userFromDB = userRepository.findById(2L).orElseThrow();
        String realUserToken = jwtService.generateToken(userFromDB);
        FlashcardSet set = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(userFromDB)
                        .build());

        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(set)
                .build();
        Flashcard savedCard = flashcardRepository.save(flashcard);

        // when, then

        mockMvc.perform(post("/flashcards/" + savedCard.getFlashcardId() + "/starred")
                        .header("Authorization", "Bearer " + realUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.starred").value(true));
    }

    @Test
    @DisplayName("DELETE /flashcards/{id}/starred - Should unstar a flashcard")
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

        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(set)
                .build();

        Flashcard savedCard = flashcardRepository.save(flashcard);

        // first, star the flashcard to ensure the relation exists
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
}
