package com.heri2go.chat.domain.chat;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {

    Flux<Chat> findByRoomNumOrderByCreatedAt(Long roomNum);
}
