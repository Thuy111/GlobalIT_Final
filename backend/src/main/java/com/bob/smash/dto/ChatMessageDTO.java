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

    @JsonProperty("message")
    private String message;

    @JsonProperty("messageType")
    private MessageType type;

    private String roomId;

    private String sender;

    private String senderNickname;

    private LocalDateTime time;
}