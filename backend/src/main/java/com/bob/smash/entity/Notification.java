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
    
    @Column(length = 1000, nullable = false)
    private String notice;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 10, nullable = false)
    private TargetType targetType;

    @Column(name = "target_idx", nullable = false)
    private Integer targetIdx;
    
    public enum TargetType {
        request, estimate, review
    }
}