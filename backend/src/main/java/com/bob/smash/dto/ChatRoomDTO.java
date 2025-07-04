package com.bob.smash.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatRoomDTO {

    private String roomId;

    private String memberUser; // 나
    private String memberNickname; // 나의 닉네임

    private String partnerUser; // 상대방
    private String partnerNickname; // 상대방의 닉네임

    private String name; // 채팅방 이름

    private ChatMessageDTO lsatMessage; // 마지막 메시지

    private LocalDateTime createdAt; // 방 생성 시간
}