package com.heri2go.chat.web.service.auth;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService{

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(user -> (UserDetails) new UserDetailsImpl(user))
            .switchIfEmpty(Mono.error(
                new UsernameNotFoundException("User not found")
            ))
            .onErrorMap(e -> {
                if (e instanceof DataAccessException) {
                    return new RuntimeException("Database error occurred", e);
                }
                return e;
            });
    }
}
