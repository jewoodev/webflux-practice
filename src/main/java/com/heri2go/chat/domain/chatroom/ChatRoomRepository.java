package com.heri2go.chat.domain.chatroom;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ChatRoomRepository extends ReactiveMongoRepository<ChatRoom, String> {

}
