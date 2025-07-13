package com.bob.smash.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @Column(length = 36) // UUID 등 사용 시 길이 확보
    private String roomId;

    @Column(nullable = false)
    private String memberUser;

    @Column(nullable = false)
    private String partnerUser;

    private String name; // 방 이름

    @Column(nullable = false)
    private LocalDateTime createdAt; // 방 생성 시간
}