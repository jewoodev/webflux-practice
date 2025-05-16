package com.heri2go.chat.web.controller.auth.request;


import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.validator.Enum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRegisterRequest(
        @Size(min = 3, max = 20, message = "Username은 3 ~ 20 자로 구성되어야 합니다.")
        @NotBlank(message = "Username은 필수 값입니다.")
        String username,

        @Size(min = 6, message = "Password는 적어도 6자보다 길어야 합니다.")
        @NotBlank(message = "Password는 필수 값입니다.")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        String email,

        @Enum(message = "유효하지 않은 역할 값입니다.")
        Role role
) {
        public static UserRegisterRequest withEncodedPassword(UserRegisterRequest req, String encodedPassword) {
                return UserRegisterRequest.builder()
                        .username(req.username)
                        .password(encodedPassword)
                        .email(req.email)
                        .role(req.role)
                        .build();
        }
}       