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

    private Integer price;
    private Status status;
    private PayType payType;
    private LocalDateTime paidAt;
}