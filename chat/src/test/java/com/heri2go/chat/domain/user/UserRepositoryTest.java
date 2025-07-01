package com.heri2go.chat.domain.user;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.heri2go.chat.domain.user.Role.LAB;

class UserRepositoryTest extends MongoTestSupport {

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class)
                .then(mongoTemplate.createCollection(User.class))
                .block();
    }

    @DisplayName("유저는 username 값으로 존재 여부를 확인될 수 있다.")
    @Test
    void userPresenceCanBeReferred_byUsername() {
        // given
        userRepository.save(
                User.builder()
                        .username("Test username")
                        .passwordHash("Test password")
                        .email("Test email")
                        .role(LAB)
                        .build()
        ).block();

        // when
        StepVerifier.create(userRepository.existsByUsername("Test username"))
                // then
                .expectNext(true)
                .verifyComplete();
    }

    @DisplayName("유저는 id 값으로 조회될 수 있다.")
    @Test
    void userCanBeReferred_byId() {
        // given
        String testUsername = "Test username";
        String testPassword = "Test password";
        String testEmail = "Test email";
        User savedUser = userRepository.save(
                User.builder()
                        .username(testUsername)
                        .passwordHash(testPassword)
                        .email(testEmail)
                        .role(LAB)
                        .build()
        ).block();

        // when
        StepVerifier.create(userRepository.findById(savedUser.getId()))
                // then
                .expectNextMatches(user -> user.getUsername().equals(testUsername) &&
                        user.getPasswordHash().equals(testPassword) &&
                        user.getEmail().equals(testEmail) &&
                        user.getRole().equals(LAB)
                )
                .verifyComplete();
    }

    @DisplayName("유저는 username 값으로 조회될 수 있다.")
    @Test
    void userCanBeReferred_byUsername() {
        // given
        String testUsername = "Test username";
        String testPassword = "Test password";
        String testEmail = "Test email";
        userRepository.save(
                User.builder()
                        .username(testUsername)
                        .passwordHash(testPassword)
                        .email(testEmail)
                        .role(LAB)
                        .build()
        ).block();

        // when
        StepVerifier.create(userRepository.findByUsername(testUsername))
                // then
                .expectNextMatches(user -> user.getUsername().equals(testUsername) &&
                        user.getPasswordHash().equals(testPassword) &&
                        user.getEmail().equals(testEmail) &&
                        user.getRole().equals(LAB)
                )
                .verifyComplete();
    }
}