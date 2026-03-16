package com.heri2go.chat.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnreadChatRepository extends JpaRepository<UnreadChat, Long> {

    List<UnreadChat> findAllByUnreadUsername(String username);
}
