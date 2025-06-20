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

    @Column(unique = true)
    private String impUid; // 아이엠포트 결제 고유 ID (imp_123456...)

    @Column(unique = true)
    private String merchantUid; // 상점 고유 주문번호

    @Column(nullable = false)
    private Integer suggestedPrice; // 제안된 결제 금액

    @Column
    private Integer actualPaidPrice; // 실제 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", length = 10)
    private PayType payType;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum Status {
        ready, paid, canceled
    }

    public enum PayType {
        kakao, toss, KG
    }
    
    public void changeStatus(Status status) {this.status = status;}
    public void changePaidAt(LocalDateTime paidAt) {this.paidAt = paidAt;}
    public void changeImpUid(String impUid) {this.impUid = impUid;}
    public void changeMerchantUid(String merchantUid) {this.merchantUid = merchantUid;}
    public void changeActualPaidPrice(Integer actualPaidPrice) {this.actualPaidPrice = actualPaidPrice;}
    public void changePayType(PayType payType) {this.payType = payType;}

}