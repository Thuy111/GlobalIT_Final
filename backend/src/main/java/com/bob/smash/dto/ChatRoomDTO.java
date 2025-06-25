package com.bob.smash.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {

    private String roomId;

    private String userA; // 나

    private String userB; // 상대방

    private String name; // 채팅방 이름
}