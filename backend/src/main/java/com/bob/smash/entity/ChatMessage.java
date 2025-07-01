package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    public enum MessageType {
        ENTER, TALK, QUIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지 타입 : 입장, 채팅, 나감
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // 방 번호 (ChatRoom과 연관관계.매핑)
    @Column(nullable = false)
    private String roomId;

    // 메시지 보낸 사람(로그인 ID 등)
    @Column(nullable = false)
    private String sender;

    // 메시지 내용
    @Column(columnDefinition = "TEXT")
    private String message;

    // 메시지 보낸 사람의 닉네임
    @Column(nullable = false)
    private String senderNickname;

    // 발송 시각
    @Column(nullable = false)
    private LocalDateTime time;

    // 읽음 여부 (단체 채팅방일 경우에는 읽음을 여러 사용자별로 관리할 함수 필요. 현재는 1:1 채팅방만 고려)
    @Column(nullable = false)
    private boolean isRead;

    // 메시지 저장 전 자동으로 시간 세팅
    @PrePersist
    protected void onCreate() {
        this.time = LocalDateTime.now();
    }
}