package com.brainbooster.user;


import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    // test for custom methods in repository

    @Test
    @DisplayName("existsByEmail - Should return true when email exists")
    void existsByEmail_ShouldReturnTrue() {
        // given
        String existingEmail = "testadmin123@test.pl";

        // when
        boolean exists = userRepository.existsByEmail(existingEmail);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - Should return false when email does not exist")
    void existsByEmail_ShouldReturnFalse() {
        // given
        String nonExistentEmail = "nonexistent@nowhere.com";

        // when
        boolean exists = userRepository.existsByEmail(nonExistentEmail);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByEmail - Should return User when exists")
    void findByEmail_ShouldReturnUser() {
        // given
        // email from sql script
        String existingEmail = "johndoe@example.com";

        // when
        Optional<User> result = userRepository.findByEmail(existingEmail);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo("johndoe");
        assertThat(result.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("findByEmail - Should return empty Optional when email does not exist")
    void findByEmail_ShouldReturnEmpty() {
        // given
        String nonExistentEmail = "unknown@example.com";

        // when
        Optional<User> result = userRepository.findByEmail(nonExistentEmail);

        // then
        assertThat(result).isEmpty();
    }

    // tests for methods from JpaRepository

    @Test
    @DisplayName("save - Should persist new user and generate ID")
    void save_ShouldPersistUser() {
        // given
        User newUser = TestEntities.userBuilder()
                .userId(null)
                .email("newuser@test.com")
                .nickname("newuser")
                .build();

        // when
        User savedUser = userRepository.save(newUser);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getUserId()).isGreaterThan(2L);
        assertThat(savedUser.getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    @DisplayName("findById - Should return user when ID exists")
    void findById_ShouldReturnUser() {
        // given
        Long existingId = 1L;

        // when
        Optional<User> result = userRepository.findById(existingId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("testadmin123@test.pl");
    }

    @Test
    @DisplayName("findAll - Should return all users including from SQL script")
    void findAll_ShouldReturnAllUsers() {
        // given
        User extraUser = TestEntities.userBuilder()
                .userId(null)
                .email("extrauser@test.com")
                .nickname("extrauser")
                .build();

        entityManager.persist(extraUser);
        entityManager.flush();

        // when
        List<User> allUsers = userRepository.findAll();

        // then
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(User::getNickname)
                .contains("testadmin", "johndoe", "extrauser");
    }

    @Test
    @DisplayName("update - Should modify existing user")
    void update_ShouldModifyUser() {
        // given
        User user = TestEntities.userBuilder()
                .userId(null)
                .email("usertoupdate@test.com")
                .nickname("originalNick")
                .build();

        User persisted = entityManager.persist(user);
        entityManager.flush();

        // when
        persisted.setNickname("updatedNick");
        User updatedUser = userRepository.save(persisted);

        // then
        assertThat(updatedUser.getNickname()).isEqualTo("updatedNick");


        Optional<User> userFromDB = userRepository.findById(persisted.getUserId());
        assertThat(userFromDB).isPresent();
        assertThat(userFromDB.get().getNickname()).isEqualTo("updatedNick");
    }

    @Test
    @DisplayName("deleteById - Should remove user")
    void deleteById_ShouldRemoveUser() {
        // given
        User user = TestEntities.userBuilder()
                .userId(null)
                .email("usertodelete@test.com")
                .nickname("usertodelete")
                .build();

        User persisted = entityManager.persist(user);
        Long idToDelete = persisted.getUserId();
        entityManager.flush();

        // when
        userRepository.deleteById(idToDelete);

        // then
        Optional<User> result = userRepository.findById(idToDelete);
        assertThat(result).isEmpty();
    }

}
