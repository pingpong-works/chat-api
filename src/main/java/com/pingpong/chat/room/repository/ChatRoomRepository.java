package com.pingpong.chat.room.repository;

import com.pingpong.chat.room.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableJpaRepositories
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByChatRoomId(String chatRoomId);
    List<ChatRoom> findByParticipantsUserId(String userId);
}
