package com.bob.smash.dto;

import lombok.*;

import java.time.LocalDateTime;
import com.bob.smash.entity.ChatMessage.MessageType;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatMessageDTO {

    private Long id;

    @JsonProperty("message") // n/과 같이 JSON에서 "message"로 매핑
    private String message;

    @JsonProperty("messageType") // enum 타입을 JSON에서 "messageType"으로 매핑
    private MessageType type;

    private String roomId;

    private String sender;

    private String senderNickname;

    private LocalDateTime time;

    private boolean isRead;
}