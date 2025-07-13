package com.bob.smash.dto;

import java.time.LocalDateTime;

import com.bob.smash.entity.Payment;
import com.bob.smash.entity.Payment.PayType;
import com.bob.smash.entity.Payment.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// LAZY 안정성을 위해 Builder, Setter를 사용하지 않음
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentDTO {
    private Integer idx;

    private String memberEmail;     // Member.emailId
    private String partnerBno;      // PartnerInfo.bno
    private Integer estimateIdx;       // Estimate.idx

    private String impUid;         // 아이엠포트 결제 고유 ID (imp_123456...)
    private String merchantUid;    // 상점 고유 주문번호

    private Integer suggestedPrice; // 제안된 결제 금액
    private Integer actualPaidPrice; // 실제 결제 금액
    private Status status;
    private PayType payType;
    private LocalDateTime createdAt; // 결제 생성 시각
    private LocalDateTime paidAt; // 결제 완료 시각
    private LocalDateTime canceledAt; // 결제 취소 시각

    public PaymentDTO(Payment payment) {
        this.idx = payment.getIdx();
        this.impUid = payment.getImpUid();
        this.merchantUid = payment.getMerchantUid();
        this.suggestedPrice = payment.getSuggestedPrice();
        this.actualPaidPrice = payment.getActualPaidPrice();
        this.status = payment.getStatus();
        this.payType = payment.getPayType();
        this.createdAt = payment.getCreatedAt();
        this.paidAt = payment.getPaidAt();
        this.canceledAt = payment.getCanceledAt();

        // Lazy 객체 초기화 (여기서 실제로 로딩됨)
        this.memberEmail = payment.getMember().getEmailId();
        this.partnerBno = payment.getPartnerInfo().getBno();
        this.estimateIdx = payment.getEstimate().getIdx();
    }
}