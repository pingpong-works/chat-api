package com.pingpong.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingpong.chat.message.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, List<ChatMessageDto>> chatMessagesMap = new ConcurrentHashMap<>();

    @KafkaListener(topics = "one-to-one-chat", groupId = "chat-group")
    public void consumeOne(ConsumerRecord<String, String> consumerRecord, Consumer<?, ?> consumer) {
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(consumerRecord.value(), ChatMessageDto.class);
            String chatRoomId = chatMessageDto.getChatRoomId();

            chatMessagesMap.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(chatMessageDto);

            messagingTemplate.convertAndSend("/topic/messages/" + chatRoomId, chatMessageDto);
            log.info("일대일 메시지 수신 및 전송 : {}", chatMessageDto);
        } catch (Exception e) {
            log.error("일대일 메시지를 처리하는 중 오류가 발생했습니다.", e);
        }
    }

    @KafkaListener(topics = "one-to-many-chat", groupId = "chat-group")
    public void consumeMany(ConsumerRecord<String, String> consumerRecord, Consumer<?, ?> consumer) {
        try {
            ChatMessageDto chatMessageDto = objectMapper.readValue(consumerRecord.value(), ChatMessageDto.class);
            String chatRoomId = chatMessageDto.getChatRoomId();

            chatMessagesMap.computeIfAbsent(chatRoomId, k -> new ArrayList<>()).add(chatMessageDto);

            messagingTemplate.convertAndSend("/topic/group/" + chatRoomId, chatMessageDto);
            log.info("일대다 메시지 수신 및 전송 : {}", chatMessageDto);
        } catch (Exception e) {
            log.error("일대다 메시지를 처리하는 중 오류가 발생했습니다.", e);
        }
    }

    public List<ChatMessageDto> getChatHistory(String chatRoomId) {
        return chatMessagesMap.getOrDefault(chatRoomId, new ArrayList<>());
    }
}