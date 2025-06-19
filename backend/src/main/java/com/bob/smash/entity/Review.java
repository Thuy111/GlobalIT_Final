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
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_idx")
    private Estimate estimate;

    @Column(length = 5, nullable = false)
    private Byte star;

    @Column(length = 3000, nullable = false)
    private String comment;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "is_modify", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isModify=0; // 0: 미 수정, 1: 수정됨

    public void changeStar(Byte star) {this.star = star;}
    public void changeComment(String comment) {this.comment = comment;}
    public void changeIsModify(Byte isModify) {this.isModify = isModify;}
}