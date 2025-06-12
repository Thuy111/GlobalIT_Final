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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_bno", nullable = false)
    private PartnerInfo partnerInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_idx", nullable = false)
    private Estimate estimate;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", length = 10, nullable = false)
    private PayType payType;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum Status {
        ready, paid, cancled
    }

    public enum PayType {
        kakao, toss
    }
    
    public void changeStatus(Status status) {this.status = status;}
}