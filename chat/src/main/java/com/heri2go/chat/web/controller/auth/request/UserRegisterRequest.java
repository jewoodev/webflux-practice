package com.heri2go.chat.web.controller.auth.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserRegisterRequest(
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters") 
        @NotBlank(message = "Username is required") 
        String username,

        @Size(min = 6, message = "Password must be at least 6 characters") 
        @NotBlank(message = "Password is required") 
        String password,

        @Email(message = "Invalid email format") 
        @NotBlank(message = "Email is required") 
        String email,

        @NotBlank(message = "Role is required")
        String role
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