package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ChatRoomServiceTest extends MockTestSupport {

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatRoomParticipantRepository chatRoomParticipantRepository;

    @InjectMocks
    ChatRoomService chatRoomService;

    private static final String TEST_USERNAME = "user2";
    private final LocalDateTime now = LocalDateTime.now();

    @DisplayName("유저는 본인의 인증 정보를 통해 자신이 참여 중인 채팅방의 정보를 조회할 수 있다.")
    @Test
    void userCanGetChatRoomInfoThroughAuthentication() {
        // given
        UserDetailsImpl testUser = new UserDetailsImpl(
                User.builder()
                        .username(TEST_USERNAME)
                        .role(Role.LAB)
                        .build());

        ChatRoomParticipant chatRoomParticipant = ChatRoomParticipant.builder()
                .username(TEST_USERNAME)
                .chatRoomId("test room")
                .build();

        ChatRoomCreateRequest createRequest1 = ChatRoomCreateRequest.builder()
                .roomName("test chat room 1")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(now.minusHours(1))
                .build();

        // createRequest1 으로 생성한 chatRoom1
        ChatRoom chatRoom1 = ChatRoom.from(createRequest1);

        // createRequest1 으로 부터 생성될 chatRoomResponse
        ChatRoomResponse chatRoomResponse1 = ChatRoomResponse.builder()
                .roomName("test chat room 1")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(now.minusHours(1))
                .build();

        ChatRoomResponse chatRoomResponse2 = ChatRoomResponse.builder()
                .roomName("test chat room 2")
                .participantIds(Set.of("user1", "user3"))
                .lastMessage("last message 2")
                .lastSender("last sender 2")
                .lastMessageTime(now.minusHours(2))
                .build();

        // when, 유저의 인증 정보를 기반으로 참여 중인 채팅방 식별자를 읽어들이고, 그 식별자를 통해 채팅방 정보를 가져오는 흐름
        when(chatRoomParticipantRepository.findAllByUsername(TEST_USERNAME)).thenReturn(Flux.just(chatRoomParticipant));
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
}