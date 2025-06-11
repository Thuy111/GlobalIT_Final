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
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    // @ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 1000, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "use_date", nullable = false)
    private LocalDateTime useDate;

    @Column(name = "use_region", length = 50, nullable = false)
    private String useRegion;

    @Column(name = "is_done", nullable = false)
    private Byte isDone;

    @Column(name = "is_get", nullable = false)
    private Byte isGet;
}