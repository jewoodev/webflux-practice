package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static com.heri2go.chat.domain.user.Role.LAB;

class ChatRoomServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";
    private final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class)
                .then(mongoTemplate.createCollection(User.class))
                .then(mongoTemplate.dropCollection(ChatRoom.class))
                .then(mongoTemplate.createCollection(ChatRoom.class))
                .then(mongoTemplate.dropCollection(ChatRoomParticipant.class))
                .then(mongoTemplate.createCollection(ChatRoomParticipant.class))
                .then(redisDao.delete("user::" + testUsername))
                .block();
    }

    @DisplayName("유저는 본인의 인증 정보를 통해 자신이 참여 중인 채팅방의 정보를 조회할 수 있다.")
    @Test
    void userCanGetChatRoomInfo_whatTheyReferring_ThroughUserDetails() {
        // given
        String testPassword = "Test password";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        String testUserId = userDetails.getUserId();

        ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
                .roomName("Test chat room 1")
                .orderId("Test order id 1")
                .participantIds(Set.of(testUserId))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        chatRoomService.save(request)
                .block();

        // when // then
        StepVerifier.create(chatRoomService.getOwnChatRoomResponse(userDetails)
                        .log()
                )
                .expectNextMatches(response -> response.roomName().equals(request.roomName()) &&
                        response.participantIds().contains(testUserId) &&
                        response.lastMessage().equals(request.lastMessage()) &&
                        response.lastSender().equals(request.lastSender()) &&
                        response.lastMessageTime().equals(request.lastMessageTime())
                )
                .verifyComplete();
    }

    @DisplayName("주문이 생성되면 채팅방이 생성된다.")
    @Test
    void chatRoom_isCreated_when_order_isCreated() {
        // given
        String testPassword = "Test password";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        String testUserId = userDetails.getUserId();

        String testOrderId = "Test order id";
        String testChatRoomId = "test chat room 1";

        ChatRoomCreateRequest requestFromOrder = ChatRoomCreateRequest.builder()
                .orderId(testOrderId)
                .roomName(testChatRoomId)
                .participantIds(Set.of(testUserId))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        chatRoomService.save(requestFromOrder)
                .block();

        // when // then
        StepVerifier.create(chatRoomService.save(requestFromOrder))
                .expectNextMatches(response ->  response.orderId().equals(requestFromOrder.orderId()) &&
                        response.roomName().equals(requestFromOrder.roomName()) &&
                        response.participantIds().equals(requestFromOrder.participantIds()) &&
                        response.lastMessage().equals(requestFromOrder.lastMessage()) &&
                        response.lastSender().equals(requestFromOrder.lastSender()) &&
                        response.lastMessageTime().equals(requestFromOrder.lastMessageTime()))
                .verifyComplete();
    }
}