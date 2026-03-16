package com.heri2go.chat.domain.user;

import com.heri2go.chat.JpaTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends JpaTestSupport {

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("유저는 username 값으로 존재 여부를 확인될 수 있다.")
    @Test
    void userPresenceCanBeReferred_byUsername() {
        // given
        userRepository.save(
                User.builder()
                        .username("Test username")
                        .password("Test password")
                        .email("Test email")
                        .role(LAB)
                        .build()
        );

        // when
        boolean exists = userRepository.existsByUsername("Test username");

        // then
        assertThat(exists).isTrue();
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
                        .password(testPassword)
                        .email(testEmail)
                        .role(LAB)
                        .build()
        );

        // when
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUsername);
        assertThat(foundUser.get().getPassword()).isEqualTo(testPassword);
        assertThat(foundUser.get().getEmail()).isEqualTo(testEmail);
        assertThat(foundUser.get().getRole()).isEqualTo(LAB);
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
                        .password(testPassword)
                        .email(testEmail)
                        .role(LAB)
                        .build()
        );

        // when
        Optional<User> foundUser = userRepository.findByUsername(testUsername);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUsername);
        assertThat(foundUser.get().getPassword()).isEqualTo(testPassword);
        assertThat(foundUser.get().getEmail()).isEqualTo(testEmail);
        assertThat(foundUser.get().getRole()).isEqualTo(LAB);
    }
}
