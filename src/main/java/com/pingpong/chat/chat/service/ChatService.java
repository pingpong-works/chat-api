package com.pingpong.chat.chat.service;

import com.pingpong.chat.message.dto.ChatMessageDto;
import com.pingpong.chat.room.entity.ChatRoom;
import com.pingpong.chat.room.repository.ChatRoomRepository;
import com.pingpong.user.dto.ChatUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void processMessage(ChatMessageDto message) {
        String kafkaTopic = message.getTopic();

        if (kafkaTopic != null && !kafkaTopic.isEmpty()) {
            if (kafkaTopic.equals("one")){
                kafkaTemplate.send("one-to-one-chat", message.getChatRoomId(), message);
                log.info("ChatService one to one : {}", kafkaTopic);
            } else if(kafkaTopic.equals("many")) {
                kafkaTemplate.send("one-to-many-chat", message.getChatRoomId(), message);
                log.info("ChatService one to many: {}", kafkaTopic);
            }
        } else {
            log.error("ChatService processMessage 실패");
        }

        if (message.getFileUrl() != null && !message.getFileUrl().isEmpty()) {
            System.out.println("File attached: " + message.getFileUrl());
        }
        updateChatRoomInfo(message);
    }

    private void updateChatRoomInfo(ChatMessageDto message) {
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(message.getChatRoomId());

        if (chatRoom == null) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + message.getChatRoomId());
        }

        // 마지막 메시지와 마지막 활성화 시간 업데이트
        chatRoom.setLastMessage(message.getContent());
        chatRoom.setLastActive(LocalDateTime.now());

        List<ChatUserDto> participantDtos = chatRoom.getParticipants()
                .stream()
                .map(participant -> new ChatUserDto(participant.getUserId(), participant.getName(), participant.getProfile())) // UserEntity -> UserDto 변환
                .collect(Collectors.toList());

        for (ChatUserDto participantDto : participantDtos) {
            if (!participantDto.getUserId().equals(message.getSenderId())) {
                chatRoom.incrementUnreadCount(participantDto.getUserId());
            }
        }

        chatRoomRepository.save(chatRoom);

        log.info("채팅방 정보 업데이트: 채팅방 ID = {}, 마지막 메시지 = {}, 마지막 활성화 시간 = {}",
                chatRoom.getChatRoomId(), chatRoom.getLastMessage(), chatRoom.getLastActive());
    }
}
