package com.bob.smash.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    public enum MessageType {
        ENTER, TALK, QUIT
    }

    private Long id;

    private MessageType type;

    private String roomId;

    private String sender;

    private String message;

    private LocalDateTime time;
}