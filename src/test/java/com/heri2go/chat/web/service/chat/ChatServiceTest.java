package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.exception.ResourceNotFoundException;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";
    private Long testRoomId;

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();
        redisDao.delete("UserResp::" + testUsername);
        if (testRoomId != null) {
            redisDao.delete("ChatPI::" + testRoomId);
        }
    }

    @DisplayName("유효한 메세지 저장 요청을 받으면 저장에 성공하고 응답 dto를 반환한다.")
    @Test
    void saveSuccessfulThroughValidRequest() {
        //Given
        String testPassword = "Test password";
        Long testRoomId = 101L;
        String chatContent = "Hello, WebFlux!";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        // When
        Chat savedChat = chatService.save(validChatCreateRequest);

        // Then
        assertThat(savedChat.getRoomId()).isEqualTo(validChatCreateRequest.roomId());
        assertThat(savedChat.getSender()).isEqualTo(validChatCreateRequest.sender());
        assertThat(savedChat.getContent()).isEqualTo(validChatCreateRequest.content());
    }

    @DisplayName("유저는 자신이 참여 중이며 존재하는 채팅방의 채팅들은 조회할 수 있다.")
    @Test
    void userCanGetChat_whatValid_andTheyParticipatedIn() {
        // Given
        String testPassword = "Test password";
        String chatContent = "Hello, WebFlux!";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        // 유저는 채팅을 치기 위해선 참여 중인 채팅방이 필요하다.

        Set<Long> participantIds = new HashSet<>();
        participantIds.add(userDetails.getUserId());

        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(participantIds)
                .build();

        ChatRoomResponse chatRoomResponse = chatRoomService.save(chatRoomCreateRequest);
        testRoomId = chatRoomResponse.id();

        // 참여 중인 채팅방이 있을 때 채팅이 저장될 수 있다.

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        chatService.save(validChatCreateRequest);

        // When
        List<ChatResponse> responses = chatService.getByRoomIdToInvited(testRoomId, userDetails);

        // Then
        assertThat(responses).hasSize(1);
        ChatResponse resp = responses.get(0);
        assertThat(resp.roomId()).isEqualTo(testRoomId);
        assertThat(resp.sender()).isEqualTo(testUsername);
        assertThat(resp.content()).isEqualTo(chatContent);
    }

    @DisplayName("채팅이 없는 채팅방을 조회하면 실패한다.")
    @Test
    void getEmptyChat_IfNonChatRoomIsQueried() {
        // Given
        String testPassword = "Test password";
        String chatContent = "Hello, WebFlux!";
        testRoomId = 101L;

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        chatService.save(validChatCreateRequest);

        // When // Then
        assertThatThrownBy(() -> chatService.getByRoomIdToInvited(999L, userDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
