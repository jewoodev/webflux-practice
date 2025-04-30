package com.heri2go.chat.web.util;


import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.auth.JwtService;

public class TestJwtTokenUtil {
    private final JwtService jwtService;

    public TestJwtTokenUtil(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    public UserDetailsImpl createTestUserDetails(String username) {
        User user = User.builder()
                .username(username)
                .password("password")
                .role(Role.LAB)
                .build();
        return new UserDetailsImpl(user);
    }

    public String getAuthorizationHeader(String username) {
        return "Bearer " + generateToken(username);
    }
}