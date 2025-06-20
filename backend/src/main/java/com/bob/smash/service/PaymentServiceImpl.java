package com.bob.smash.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Payment;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PartnerInfoRepository partnerInfoRepository;
    private final EstimateRepository estimateRepository;

    // 결제 정보 저장
    @Override
    public Payment savePayment(String memberEmail,
                              String partnerBno,
                              Integer estimateIdx,
                              Integer price) {

        Member member = memberRepository.findByEmailId(memberEmail)
                            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다"));

        PartnerInfo partnerInfo = partnerInfoRepository.findByBno(partnerBno)
                            .orElseThrow(() -> new IllegalArgumentException("파트너 정보를 찾을 수 없습니다"));

        Estimate estimate = estimateRepository.findById(estimateIdx)
                            .orElseThrow(() -> new IllegalArgumentException("견적 정보를 찾을 수 없습니다"));

        Payment payment = Payment.builder()
                                .suggestedPrice(price)
                                .actualPaidPrice(0) // 초기값은 0, 실제 결제 후 업데이트
                                .status(Payment.Status.ready)
                                .createdAt(LocalDateTime.now())
                                .member(member) // DB에서 조회한 Member
                                .partnerInfo(partnerInfo) // DB에서 조회한 PartnerInfo
                                .estimate(estimate) // DB에서 조회한 Estimate
                                .build();

      return paymentRepository.save(payment);
    }

    // 아이엠포트 결제 정보로 Payment 엔티티 정보 추가 저장
    @Override
    @Transactional
    public Payment savePaymentFromIamport(com.siot.IamportRestClient.response.Payment impPayment,  Integer estimateIdx) {

        Payment payment = paymentRepository.findByEstimate_Idx(estimateIdx)
        .orElseThrow(() -> new IllegalArgumentException("결제 내역이 없습니다"));

        payment.changeImpUid(impPayment.getImpUid());
        payment.changeMerchantUid(impPayment.getMerchantUid());
        payment.changeActualPaidPrice(impPayment.getAmount() != null ? impPayment.getAmount().intValue() : 0);
        payment.changePayType(convertPayType(impPayment.getPayMethod()));
        payment.changeStatus(Payment.Status.paid);
        payment.changePaidAt(impPayment.getPaidAt() != null
                        ? impPayment.getPaidAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null);

        return paymentRepository.save(payment);
    }

    // 결제 취소
    @Override
    @Transactional
    public Payment cancelPayment(Integer paymentIdx) {
        Payment payment = paymentRepository.findById(paymentIdx)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        payment.changeStatus(Payment.Status.canceled);
        payment.changePaidAt(null); // 결제 취소 시 paidAt도 null로 설정

        return paymentRepository.save(payment);
    }

    // 문자열로 받은 상태 → Payment.Status enum 변환
    // private Payment.Status convertStatus(String statusStr) {
    //     if (statusStr == null) return Payment.Status.ready; // 기본값
    //     switch(statusStr.toLowerCase()) {
    //         case "paid": return Payment.Status.paid;
    //         case "ready": return Payment.Status.ready;
    //         case "cancelled": 
    //         case "canceled":
    //             return Payment.Status.canceled;
    //         default: return Payment.Status.ready;
    //     }
    // }

    // 문자열로 받은 결제수단 → Payment.PayType enum 변환
    private Payment.PayType convertPayType(String payTypeStr) {
        if (payTypeStr == null) return Payment.PayType.kakao; // 기본값
        switch(payTypeStr.toLowerCase()) {
            case "kakaopay": return Payment.PayType.kakao;
            case "tosspay": return Payment.PayType.toss;
            case "html5_inicis": return Payment.PayType.KG;
            default: return Payment.PayType.kakao;
        }
    }
}
