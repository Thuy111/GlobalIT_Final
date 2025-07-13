package com.bob.smash.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Payment;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.PaymentRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.IamportClient;

import jakarta.annotation.PostConstruct;
// import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PartnerInfoRepository partnerInfoRepository;
    private final EstimateRepository estimateRepository;
    private IamportClient iamportClient;
    
    @Value("${imp.api.secretkey}")
    private String secretKey;
    
    @Value("${imp.api.key}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    // 결제 정보 저장
    @Override
    public PaymentDTO savePayment(String memberEmail,
                              String partnerBno,
                              Integer estimateIdx,
                              Integer price) {

        System.out.println("결제 정보::: " + memberEmail + ", partnerBno: " + partnerBno + ", estimateIdx: " + estimateIdx + ", price: " + price);
        
        Member member = memberRepository.findByEmailId(memberEmail)
                            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다"));

        PartnerInfo partnerInfo = partnerInfoRepository.findByBno(partnerBno)
                            .orElseThrow(() -> new IllegalArgumentException("파트너 정보를 찾을 수 없습니다"));

        Estimate estimate = estimateRepository.findById(estimateIdx)
                            .orElseThrow(() -> new IllegalArgumentException("견적 정보를 찾을 수 없습니다"));

        // 중복 결제서 생성 방지(estimateIdx가 이미 존재하면. 하나의 견적서에 복수의 결제서가 생성되면 예외 발생)
        List<Payment> existingPayments = paymentRepository.findByEstimate_Idx(estimateIdx);
        // canceled 상태의 결제서는 제외
        existingPayments.removeIf(payment -> payment.getStatus() == Payment.Status.canceled);
        if (!existingPayments.isEmpty()) {
            throw new IllegalArgumentException("이미 해당 견적서에 대한 결제 정보가 존재합니다.");
        }

        Payment payment = Payment.builder()
                                .suggestedPrice(price)
                                .actualPaidPrice(0) // 초기값은 0, 실제 결제 후 업데이트
                                .status(Payment.Status.ready)
                                .createdAt(LocalDateTime.now())
                                .member(member) // DB에서 조회한 Member
                                .partnerInfo(partnerInfo) // DB에서 조회한 PartnerInfo
                                .estimate(estimate) // DB에서 조회한 Estimate
                                .build();
        Payment savedPayment = paymentRepository.save(payment); // Payment 엔티티 저장
        PaymentDTO paymentDTO = entityToDto(savedPayment); // Payment 엔티티를 DTO로 변환
      return paymentDTO;
    }

    // 아이엠포트 결제 정보로 Payment 엔티티 정보 추가 저장
    @Override
    @Transactional
    public Payment savePaymentFromIamport(com.siot.IamportRestClient.response.Payment impPayment,  Integer estimateIdx, String payType, String impUid) {
        System.out.println("아이엠포트 결제 타입::: " + impPayment.getPayMethod());
        System.out.println("PG사::: " + impPayment.getPgProvider());

        List<Payment> payments = paymentRepository.findByEstimate_Idx(estimateIdx);
        Optional<Payment> paidPayment = payments.stream()
        .filter(p -> p.getStatus() == Payment.Status.paid) // 이미 결제된 상태인 경우
        .findFirst();
        // Optional<Payment> readyPayment = payments.stream() 
        // .filter(p -> p.getStatus() == Payment.Status.ready) // 결제 준비 중인 상태인 경우
        // .findFirst();
        
        // 결제서 idx 조회 (ready 상태인 결제서가 있는지 확인)
        Integer paymentIdx = paymentRepository.findReadyIdxByEstimateIdx(estimateIdx);
        if (paymentIdx == null) {
            throw new IllegalArgumentException("결제 준비중의 결제서가 없습니다.");
        }
        System.out.println("결제서 idx: " + paymentIdx);

        if (payments.isEmpty()) {
            // 결제 취소
            cancelPayment(paymentIdx, true, impUid);
            throw new IllegalArgumentException("해당 견적서에 대한 결제 정보가 없습니다. 결제 취소 처리되었습니다.");
        }else if (paidPayment.isPresent()) {
            cancelPayment(paymentIdx, true, impUid);
            throw new IllegalStateException("이미 결제된 견적서입니다. 결제 취소 처리되었습니다.");
        }
        // else if (readyPayment.isPresent()) {
        //     cancelPayment(paymentIdx);
        //     throw new IllegalStateException("이미 결제 준비 중인 견적서입니다. 결제 취소 처리되었습니다.");
        // }

        Optional<Payment> maybePayment = paymentRepository.findReadyPaymentByEstimateIdx(estimateIdx);
        if (maybePayment.isEmpty()) {
            // 결제 취소 처리
            cancelPayment(paymentIdx, true, impUid);
            throw new IllegalArgumentException("결제 내역이 없습니다. 결제 취소 처리되었습니다.");
        }

        Payment payment = maybePayment.get(); // Optional에서 Payment 객체 추출

        payment.changeImpUid(impPayment.getImpUid());
        payment.changeMerchantUid(impPayment.getMerchantUid());
        payment.changeActualPaidPrice(impPayment.getAmount() != null ? impPayment.getAmount().intValue() : 0);
        payment.changePayType(convertPayType(payType));
        payment.changeStatus(Payment.Status.paid);
        payment.changePaidAt(impPayment.getPaidAt() != null
                        ? impPayment.getPaidAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null);

        return paymentRepository.save(payment);
    }

    // 결제 취소 (DB 상태 업데이트 + 아임포트 환불 요청)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 다른 함수에서 예외 발생 시 롤백이 일어나지 않도록 (독립 트랜잭션)
    public Payment cancelPayment(Integer paymentIdx, Boolean isAutoCancel, String impUid) {

        Payment payment = paymentRepository.findById(paymentIdx)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        if(isAutoCancel){
            // isAutoCancel == true인 경우, 결제 상태가 'canceled'이면 예외 발생, ready 상태는 취소 가능
            if (payment.getStatus() == Payment.Status.canceled) {
                throw new IllegalStateException("이미 취소된 결제서므로 환불할 수 없습니다.");
            }
        } else {
            // isAutoCancel == false인 경우, 결제 상태가 'paid'인 경우에만 취소 가능. 이외의 상태에서는 예외 발생
            if (payment.getStatus() != Payment.Status.paid) {
                throw new IllegalStateException("결제되지 않았거나 이미 취소된 결제서입니다.");
            }
        }

        String uid = impUid;
        System.out.println("uid = " + uid);
        if(uid == null || uid.isEmpty()) {
            uid = payment.getImpUid();
            System.out.println("null uid = " + uid);
        }
        System.out.println("accept uid = " + uid);

        if (uid == null || uid.isEmpty()) { // 그럼에도 impUid가 없으면 예외 발생
            throw new IllegalStateException("impUid가 존재하지 않아 환불을 처리할 수 없습니다.");
        }
        
        try {
            // 아임포트 환불 요청
            refundPayment(uid);

            // DB 상태 업데이트
            payment.changeStatus(Payment.Status.canceled);
            payment.changeCanceledAt(LocalDateTime.now());

            return paymentRepository.save(payment);

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("아임포트 환불 처리 중 오류가 발생했습니다.");
        }
    }

    // 결제서 DB 닫기
    @Override
    @Transactional
    public void closePayment(Integer paymentIdx) {
        Payment payment = paymentRepository.findById(paymentIdx)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        // 결제 상태가 'paid' 또는 'canceled' 경우에는 닫기 불가
        if (payment.getStatus() != Payment.Status.ready ) {
            throw new IllegalStateException("이미 결제된 결제되거나 취소된 결제서는 취소할 수 없습니다.");
        }

        // DB 상태 업데이트
        payment.changeStatus(Payment.Status.canceled);
        payment.changeCanceledAt(LocalDateTime.now());

        paymentRepository.save(payment);        
    }

    // 결제서 조회
    @Override
    public PaymentDTO getPaymentByIdx(Integer paymentIdx) {
        Payment payment = paymentRepository.findById(paymentIdx)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        return entityToDto(payment);
    }

    // 결제서 목록 조회
    @Override
    public List<PaymentDTO> allFindPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                       .map(this::entityToDto)
                       .toList();
    }

    // 회원별 결제서 목록 조회
    @Override
    public List<PaymentDTO> getAllPaymentsByMemberEmail(String memberEmail) {
        memberRepository.findByEmailId(memberEmail)
            .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다"));

        List<Payment> payments = paymentRepository.findByMember_EmailId(memberEmail);
        return payments.stream()
                       .map(this::entityToDto)
                       .toList();
    }

    // bno 로 결제서 목록 조회
    @Override
    public List<PaymentDTO> getAllPaymentsByBno(String partnerBno) {
        if (partnerBno == null || partnerBno.isEmpty()) {
            throw new IllegalArgumentException("파트너 정보를 찾을 수 없습니다");
        }

        List<Payment> payments = paymentRepository.findByPartnerInfo_Bno(partnerBno);
        return payments.stream()
                    .map(this::entityToDto)
                    .toList();
    }

    // 문자열로 받은 결제수단 → Payment.PayType enum 변환
    private Payment.PayType convertPayType(String payTypeStr) {
        if (payTypeStr == null) return Payment.PayType.kakao; // 기본값
        switch(payTypeStr.toLowerCase()) {
            case "kakao": return Payment.PayType.kakao;
            case "toss": return Payment.PayType.toss;
            case "KG": return Payment.PayType.KG;
            default: return Payment.PayType.KG;
        }
    }

    // 환불
    @Override
    public void refundPayment(String impUid) {
        try {
            // 아임포트 환불 요청
            CancelData cancelData = new CancelData(impUid, true); // 전체 환불
            cancelData.setReason("사용자 요청 환불"); // 선택사항
            IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.cancelPaymentByImpUid(cancelData);

            if (!"cancelled".equals(iamportResponse.getResponse().getStatus())) {
                throw new IllegalStateException("환불 처리에 실패했습니다.");
            }

        } catch (IamportResponseException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("아임포트 환불 처리 중 오류가 발생했습니다.");
        }
    }
}
