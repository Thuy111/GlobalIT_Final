package com.bob.smash.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ReadEventDTO {
    private String roomId; // 채팅방 ID
    private List<Long> readMessageIds; // 읽음 처리된 메시지 ID 목록
    private String sender; // 읽음 이벤트를 보낸 사용자
}
