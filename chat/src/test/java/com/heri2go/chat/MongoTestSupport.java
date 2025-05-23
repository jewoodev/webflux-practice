package com.heri2go.chat;

import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.chat.UnreadChatRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataMongoTest
public abstract class MongoTestSupport {

    @Autowired
    protected ReactiveMongoTemplate mongoTemplate;

    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected ChatRoomRepository chatRoomRepository;

    @Autowired
    protected ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Autowired
    protected UnreadChatRepository unreadChatRepository;

    @Autowired
    protected UserRepository userRepository;
}
