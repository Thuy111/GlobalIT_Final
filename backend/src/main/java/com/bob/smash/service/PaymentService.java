package com.bob.smash.service;

import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Payment;

public interface PaymentService {
  // 결제 관련 메소드 정의
  // 예: 결제 요청, 결제 상태 조회 등


  // Dto → Entity 변환
  default Payment dtoToEntity(PaymentDTO dto) {
    Payment payment = Payment.builder()
                              .idx(dto.getIdx())
                              .member(Member.builder().emailId(dto.getMemberEmail()).build())
                              .partnerInfo(PartnerInfo.builder().bno(dto.getPartnerBno()).build())
                              .estimate(Estimate.builder().idx(dto.getEstimateIdx()).build())
                              .price(dto.getPrice())
                              .status(dto.getStatus())
                              .payType(dto.getPayType())
                              .paidAt(dto.getPaidAt())
                              .build();
    return payment;
  }

  // Entity → Dto 변환
  default PaymentDTO entityToDto(Payment entity) {
    return PaymentDTO.builder()
                     .idx(entity.getIdx())
                     .memberEmail(entity.getMember().getEmailId())
                     .partnerBno(entity.getPartnerInfo().getBno())
                     .estimateIdx(entity.getEstimate().getIdx())
                     .price(entity.getPrice())
                     .status(entity.getStatus())
                     .payType(entity.getPayType())
                     .paidAt(entity.getPaidAt())
                     .build();
  }
}