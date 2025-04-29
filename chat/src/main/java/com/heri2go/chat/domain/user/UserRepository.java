package com.heri2go.chat.domain.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    @Cacheable(value = "user", key = "#p0", cacheManager = "cacheManager", unless = "#result == null")
    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);
}