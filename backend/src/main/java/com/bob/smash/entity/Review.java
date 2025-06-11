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

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "estimate_idx", nullable = false)
    private Estimate estimate;

    @Column(nullable = false)
    private Byte star;

    @Column(length = 3000, nullable = false)
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_modify", nullable = false)
    private Byte isModify;

    public void changeStar(Byte star) {this.star = star;}
    public void changeComment(String comment) {this.comment = comment;}
    public void changeIsModify(Byte isModify) {this.isModify = isModify;}
}