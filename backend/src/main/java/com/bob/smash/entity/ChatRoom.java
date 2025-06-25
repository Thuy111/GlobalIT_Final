package com.bob.smash.entity;

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
    private String userA;

    @Column(nullable = false)
    private String userB;

    @Column(nullable = false) // 방 이름
    private String name;
}