package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserService userService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.getByUsername(username)
            .map(user -> (UserDetails) new UserDetailsImpl(user))
            .switchIfEmpty(Mono.error(
                new UsernameNotFoundException("User not found")
            ));
    }
}
