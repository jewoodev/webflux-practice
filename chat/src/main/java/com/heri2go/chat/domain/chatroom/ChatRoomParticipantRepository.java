package com.heri2go.chat.domain.chatroom;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface ChatRoomParticipantRepository extends ReactiveMongoRepository<ChatRoomParticipant, String> {

    Flux<ChatRoomParticipant> findAllByUserId(String userId);
}
