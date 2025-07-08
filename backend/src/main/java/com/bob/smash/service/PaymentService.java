package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Payment;

public interface PaymentService {
  // 결제 정보 저장
  PaymentDTO savePayment(String memberEmail,
                      String partnerBno,
                      Integer estimateIdx,
                      Integer price);
  // 아이엠포트 결제 정보로 Payment 엔티티 저장
  Payment savePaymentFromIamport(com.siot.IamportRestClient.response.Payment impPayment, Integer estimateIdx, String payType, String impUid);

  // 결제 취소
  Payment cancelPayment(Integer paymentIdx, Boolean isAutoCancel, String impUid);
  // 결제 DB 닫기
  void closePayment(Integer paymentIdx);
  // 결제 환불
  void refundPayment(String impUid);

  // 결제서 상세 조회
  PaymentDTO getPaymentByIdx(Integer paymentIdx);

  // 결제서 목록 조회
  List<PaymentDTO> getAllPaymentsByMemberEmail(String memberEmail); // 회원별 결제서 목록 조회
  List<PaymentDTO> allFindPayments();
  List<PaymentDTO> getAllPaymentsByBno(String partnerBno); // bno를 이용해 조회

  // Dto → Entity 변환
  default Payment dtoToEntity(PaymentDTO dto) {
    Payment payment = Payment.builder()
                              .idx(dto.getIdx())
                              .member(Member.builder().emailId(dto.getMemberEmail()).build())
                              .partnerInfo(PartnerInfo.builder().bno(dto.getPartnerBno()).build())
                              .estimate(Estimate.builder().idx(dto.getEstimateIdx()).build())
                              .impUid(dto.getImpUid())
                              .merchantUid(dto.getMerchantUid())
                              .suggestedPrice(dto.getSuggestedPrice())
                              .actualPaidPrice(dto.getActualPaidPrice())
                              .status(dto.getStatus())
                              .payType(dto.getPayType())
                              .createdAt(dto.getCreatedAt())
                              .paidAt(dto.getPaidAt())
                              .canceledAt(dto.getCanceledAt())
                              .build();
    return payment;
  }

  // Entity → Dto 변환
  // default PaymentDTO entityToDto(Payment entity) {
  //   return PaymentDTO.builder()
  //                     .idx(entity.getIdx())
  //                     .memberEmail(entity.getMember().getEmailId())
  //                     .partnerBno(entity.getPartnerInfo().getBno())
  //                     .estimateIdx(entity.getEstimate().getIdx())
  //                     .impUid(entity.getImpUid())
  //                     .merchantUid(entity.getMerchantUid())
  //                     .suggestedPrice(entity.getSuggestedPrice())
  //                     .actualPaidPrice(entity.getActualPaidPrice())
  //                     .status(entity.getStatus())
  //                     .payType(entity.getPayType())
  //                     .createdAt(entity.getCreatedAt())
  //                     .paidAt(entity.getPaidAt())
  //                     .canceledAt(entity.getCanceledAt())
  //                     .build();
  // }
  default PaymentDTO entityToDto(Payment entity) {
    return new PaymentDTO(
        entity.getIdx(),
        entity.getMember().getEmailId(),
        entity.getPartnerInfo().getBno(),
        entity.getEstimate().getIdx(),
        entity.getImpUid(),
        entity.getMerchantUid(),
        entity.getSuggestedPrice(),
        entity.getActualPaidPrice(),
        entity.getStatus(),
        entity.getPayType(),
        entity.getCreatedAt(),
        entity.getPaidAt(),
        entity.getCanceledAt()
    );
}
}