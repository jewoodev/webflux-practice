package com.heri2go.chat.domain.chat;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UnreadChatRepository extends ReactiveMongoRepository<UnreadChat, String> {

    Flux<UnreadChat> findByUnreadUsername(String username);
}
