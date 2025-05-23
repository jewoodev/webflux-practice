package com.heri2go.chat;

import com.heri2go.chat.domain.RedisDao;
import com.heri2go.chat.domain.chat.UnreadChatRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.token.RefreshHashRepository;
import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.UserDetailsServiceImpl;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.UnreadChatService;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    @Autowired
    protected ReactiveMongoTemplate mongoTemplate;

    @Autowired
    protected RedisDao redisDao;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected UserDetailsServiceImpl userDetailsService;

    @Autowired
    protected ChatService chatService;

    @Autowired
    protected UnreadChatService unreadChatService;

    @Autowired
    protected UnreadChatRepository unreadChatRepository;

    @Autowired
    protected ChatRoomService chatRoomService;

    @Autowired
    protected ChatRoomRepository chatRoomRepository;

    @Autowired
    protected RefreshHashRepository refreshHashRepository;
}
