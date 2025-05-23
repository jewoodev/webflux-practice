package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.exception.ResourceNotFoundException;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

import static com.heri2go.chat.domain.user.Role.LAB;

class ChatServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";
    private String testRoomId;

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(Chat.class)
                .then(mongoTemplate.createCollection(Chat.class))
                .then(mongoTemplate.dropCollection(User.class))
                .then(mongoTemplate.createCollection(User.class))
                .then(redisDao.delete("UserResp::" + testUsername))
                .then(redisDao.delete("ChatPI::" + testRoomId))
                .block();
    }

    @DisplayName("유효한 메세지 저장 요청을 받으면 저장에 성공하고 응답 dto를 반환한다.")
    @Test
    void saveSuccessfulThroughValidRequest() {
        //Given
        String testPassword = "Test password";
        String testRoomId = "101";
        String chatContent = "Hello, WebFlux!";;

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        // When // Then
        StepVerifier.create(chatService.save(validChatCreateRequest))
                .expectNextMatches(chat -> chat.getRoomId().equals(validChatCreateRequest.roomId()) &&
                        chat.getSender().equals(validChatCreateRequest.sender()) &&
                        chat.getContent().equals(validChatCreateRequest.content()))
                .verifyComplete();
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

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        // 유저는 채팅을 치기 위해선 참여 중인 채팅방이 필요하다.

        Set<String> participantIds = new HashSet<>();
        participantIds.add(userDetails.getUserId());

        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(participantIds)
                .build();

        ChatRoomResponse chatRoomResponse = chatRoomService.save(chatRoomCreateRequest)
                .block();
        testRoomId = chatRoomResponse.id();

        // 참여 중인 채팅방이 있을 때 채팅이 저장될 수 있다.

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        chatService.save(validChatCreateRequest)
                .block();

        // When
        StepVerifier.create(chatService.getByRoomIdToInvited(testRoomId, userDetails))
                // Then
                .expectNextMatches(resp -> resp.roomId().equals(testRoomId) &&
                        resp.sender().equals(testUsername) &&
                        resp.content().equals(chatContent))
                .verifyComplete();
    }

    @DisplayName("채팅이 없는 채팅방을 조회하면 실패한다.")
    @Test
    void getEmptyChat_IfNonChatRoomIsQueried() {
        // Given
        String testPassword = "Test password";
        String chatContent = "Hello, WebFlux!";;
        testRoomId = "101";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        ChatCreateRequest validChatCreateRequest = ChatCreateRequest.builder()
                .content(chatContent)
                .sender(testUsername)
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();

        chatService.save(validChatCreateRequest)
                .block();

        // Then
        StepVerifier.create(chatService.getByRoomIdToInvited("999", userDetails))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
