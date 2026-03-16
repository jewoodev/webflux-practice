package com.heri2go.chat.web.service.user;

import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.exception.UserNotFoundException;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getById(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    @Cacheable(value = "UserResp", key = "#p0", cacheManager = "cacheManager", unless = "#result == null")
    public UserResponse getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }
}
