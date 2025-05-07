package com.heri2go.chat.web.service.user;

import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.exception.UserNotFoundException;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Mono<UserResponse> getById(String userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found.")));
    }

    @Cacheable(value = "user", key = "#p0", cacheManager = "cacheManager", unless = "#result == null")
    public Mono<UserResponse> getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found.")));
    }
}
