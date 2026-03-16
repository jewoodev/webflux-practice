package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";
    private final LocalDateTime NOW = LocalDateTime.now().withNano(0);

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();
        redisDao.delete("UserResp::" + testUsername);
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

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        Long testUserId = userDetails.getUserId();

        ChatRoomCreateRequest request = ChatRoomCreateRequest.builder()
                .roomName("Test chat room 1")
                .orderId("Test order id 1")
                .participantIds(Set.of(testUserId))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        chatRoomService.save(request);

        // when
        List<ChatRoomResponse> responses = chatRoomService.getOwnChatRoomResponse(userDetails);

        // then
        assertThat(responses).hasSize(1);
        ChatRoomResponse response = responses.get(0);
        assertThat(response.roomName()).isEqualTo(request.roomName());
        assertThat(response.participantIds()).contains(testUserId);
        assertThat(response.lastMessage()).isEqualTo(request.lastMessage());
        assertThat(response.lastSender()).isEqualTo(request.lastSender());
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

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        Long testUserId = userDetails.getUserId();

        String testOrderId = "Test order id";
        String testChatRoomName = "test chat room 1";

        ChatRoomCreateRequest requestFromOrder = ChatRoomCreateRequest.builder()
                .orderId(testOrderId)
                .roomName(testChatRoomName)
                .participantIds(Set.of(testUserId))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        // when
        ChatRoomResponse response = chatRoomService.save(requestFromOrder);

        // then
        assertThat(response.orderId()).isEqualTo(requestFromOrder.orderId());
        assertThat(response.roomName()).isEqualTo(requestFromOrder.roomName());
        assertThat(response.participantIds()).isEqualTo(requestFromOrder.participantIds());
        assertThat(response.lastMessage()).isEqualTo(requestFromOrder.lastMessage());
        assertThat(response.lastSender()).isEqualTo(requestFromOrder.lastSender());
    }
}
