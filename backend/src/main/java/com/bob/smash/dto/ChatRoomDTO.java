package com.bob.smash.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatRoomDTO {

    private String roomId;

    private String myUser; // 나

    private String targetUser; // 상대방

    private String name; // 채팅방 이름
}