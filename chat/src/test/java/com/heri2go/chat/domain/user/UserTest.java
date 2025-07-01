package com.heri2go.chat.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.heri2go.chat.domain.user.Role.LAB;
import static com.heri2go.chat.domain.user.UserStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    @DisplayName("회원 엔티티의 프로퍼티는 null을 허용하지 않는다.")
    @Test
    void constructorNullCheck() {
        assertThatThrownBy(() -> User.builder()
                                    .username(null)
                                    .passwordHash(null)
                                    .email("jewoos15@naver.com")
                                    .role(LAB)
                                    .build())
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("회원은 활성 상태가 될 수 있다.")
    @Test
    void activateSuccessfully() {
        var user = User.builder()
                .username("jewoo")
                .passwordHash("passwordExample")
                .email("jewoos15@naver.com")
                .role(LAB)
                .build();
        
        user.activate();
        
        assertThat(user.getStatus()).isEqualTo(ACTIVE);
    }

    @DisplayName("회원은 활성 상태 전환을 2번 이상 할 수 없다.")
    @Test
    void activateInFailure() {
        var user = User.builder()
                .username("jewoo")
                .passwordHash("passwordExample")
                .email("jewoos15@naver.com")
                .role(LAB)
                .build();
        user.activate();

        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("PENDING 상태가 아닙니다.");
    }

    @DisplayName("회원은 비활성 상태로 전환될 수 있다.")
    @Test
    void deactivate() {
        var user = User.builder()
                .username("jewoo")
                .passwordHash("passwordExample")
                .email("jewoos15@naver.com")
                .role(LAB)
                .build();
        user.activate();

        user.deactivate();

        assertThat(user.getStatus()).isEqualTo(DEACTIVATED);
    }

    @DisplayName("회원은 '대기중' 상태에서 비활성 상태로 전환될 수 없다.")
    @Test
    void deactivateInFailure() {
        var user = User.builder()
                .username("jewoo")
                .passwordHash("passwordExample")
                .email("jewoos15@naver.com")
                .role(LAB)
                .build();

        assertThatThrownBy(() -> user.deactivate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ACTIVE 상태가 아닙니다.");
    }
}