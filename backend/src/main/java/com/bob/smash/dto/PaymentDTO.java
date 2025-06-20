package com.bob.smash.dto;

import java.time.LocalDateTime;

import com.bob.smash.entity.Payment.PayType;
import com.bob.smash.entity.Payment.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}