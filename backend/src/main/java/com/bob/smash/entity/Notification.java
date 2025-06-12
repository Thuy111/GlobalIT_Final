package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 10, nullable = false)
    private TargetType targetType;

    @Column(name = "target_idx", nullable = false)
    private Integer targetIdx;

    @Column(length = 1000, nullable = false)
    private String notice;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isRead = 0; // 0: 읽지 않음, 1: 읽음;

    public enum TargetType {
        request, estimate, review
    }

    public void changeIsRead(Byte isRead) {this.isRead = isRead;}
}