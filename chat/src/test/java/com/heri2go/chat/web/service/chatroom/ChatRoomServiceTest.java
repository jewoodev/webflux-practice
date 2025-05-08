package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import com.heri2go.chat.web.service.user.UserService;
import com.heri2go.chat.web.service.user.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ChatRoomServiceTest extends MockTestSupport {

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatRoomParticipantService chatRoomParticipantService;

    @Mock
    UserService userService;

    @InjectMocks
    ChatRoomService chatRoomService;

    private final LocalDateTime NOW = LocalDateTime.now();

    @DisplayName("유저는 본인의 인증 정보를 통해 자신이 참여 중인 채팅방의 정보를 조회할 수 있다.")
    @Test
    void userCanGetChatRoomInfoThroughAuthentication() {
        // given
        String testUsername = "user2";

        UserDetailsImpl testUser = new UserDetailsImpl(
                UserResponse.builder()
                        .username(testUsername)
                        .role(LAB)
                        .build());

        ChatRoomParticipant chatRoomParticipant = ChatRoomParticipant.builder()
                .username(testUsername)
                .chatRoomId("test room")
                .build();

        ChatRoomCreateRequest createRequest1 = ChatRoomCreateRequest.builder()
                .roomName("test chat room 1")
                .orderId("orderId1")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        // createRequest1 으로 생성한 chatRoom1
        ChatRoom chatRoom1 = ChatRoom.from(createRequest1);

        // createRequest1 으로 부터 생성될 chatRoomResponse
        ChatRoomResponse chatRoomResponse1 = ChatRoomResponse.builder()
                .roomName("test chat room 1")
                .orderId("orderId1")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(NOW.minusHours(1))
                .build();

                // 저장되어 있다고 가정하는 ChatRoom2 에 대한 response 인스턴스, 이 테스트 시나리오에서는 user2가 참여하고 있지 않은 ChatRoom이므로 조회되지 않는다. 
                // 따라서 테스트 검증 로직에쓰이지 않으므로 주석처리.
        // ChatRoomResponse chatRoomResponse2 = ChatRoomResponse.builder()
        //         .roomName("test chat room 2")
        //         .orderId("orderId2")
        //         .participantIds(Set.of("user1", "user3"))
        //         .lastMessage("last message 2")
        //         .lastSender("last sender 2")
        //         .lastMessageTime(NOW.minusHours(2))
        //         .build(); bearer 

        // when, 유저의 인증 정보를 기반으로 참여 중인 채팅방 식별자를 읽어들이고, 그 식별자를 통해 채팅방 정보를 가져오는 흐름
        when(chatRoomParticipantService.getAllByUserId(testUsername)).thenReturn(Flux.just(chatRoomParticipant));
        when(chatRoomRepository.findById(anyString())).thenReturn(Mono.just(chatRoom1));

        // then
        StepVerifier.create(chatRoomService.getOwnChatRoomResponse(testUser))
                .expectNextMatches(response -> response.roomName().equals(chatRoomResponse1.roomName()) &&
                        response.participantIds().contains("user1") &&
                        response.participantIds().contains("user2") &&
                        response.lastMessage().equals(chatRoomResponse1.lastMessage()) &&
                        response.lastSender().equals(chatRoomResponse1.lastSender()) &&
                        response.lastMessageTime().equals(chatRoomResponse1.lastMessageTime()))
                .verifyComplete();
    }

    @DisplayName("주문이 생성되면 채팅방이 생성된다.")
    @Test
    void chatRoom_isCreated_when_order_isCreated() {
        // given
        UserResponse userResp = UserResponse.builder()
                .username("test user")
                .password("encodedPassword")
                .role(LAB)
                .build();

        ChatRoomCreateRequest createRequestStartedWithOrder = ChatRoomCreateRequest.builder()
                .roomName("test chat room 1")
                .orderId("orderId1")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        // when
        when(userService.getById(any(String.class))).thenReturn(Mono.just(userResp));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(Mono.just(ChatRoom.from(createRequestStartedWithOrder)));

        // then
        StepVerifier.create(chatRoomService.save(createRequestStartedWithOrder))
                .expectNextMatches(response -> response.roomName().equals(createRequestStartedWithOrder.roomName()) &&
                        response.orderId().equals(createRequestStartedWithOrder.orderId()) &&
                        response.participantIds().equals(createRequestStartedWithOrder.participantIds()) &&
                        response.lastMessage().equals(createRequestStartedWithOrder.lastMessage()) &&
                        response.lastSender().equals(createRequestStartedWithOrder.lastSender()) &&
                        response.lastMessageTime().equals(createRequestStartedWithOrder.lastMessageTime()))
                .verifyComplete();
    }
}