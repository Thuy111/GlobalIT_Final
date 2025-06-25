package com.bob.smash.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.entity.Payment;
import com.bob.smash.service.PartnerInfoService;
import com.bob.smash.service.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse; // 사용중 > 바로 사용하고 있으므로 사용처리로 안보임

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PartnerInfoService partnerInfoService;

    @Value("${imp.api.key}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;

    @Value("${imp.code}")
    private String impMerchantCode;

    // Iamport API 클라이언트 설정
    // Iamport API 키와 시크릿 키는 application.properties 파일에서 관리
    private IamportClient iamportClient;

    // @PostConstruct 어노테이션을 사용하여 애플리케이션 시작 시 IamportClient를 초기화
    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    @GetMapping("")
    public String index(Model model) {
        return "redirect:/smash/payment/test"; // 결제 페이지로 리다이렉트
    }
    // 결제 테스트 페이지
    @GetMapping("/test")
    public String paymentPage(Model model) {
        model.addAttribute("impMerchantCode", impMerchantCode);
        // 다른 필요한 데이터 추가
        return "smash/payment/test"; // 결제 테스트 페이지로 이동
    }
    // 결제 페이지
    @GetMapping("/pay/{paymentIdx}")
    public String paymentPage(@PathVariable Integer paymentIdx, Model model) {
        try {
            PaymentDTO payment = paymentService.getPaymentByIdx(paymentIdx);
            if (payment != null) {
                System.out.println("partner = " + partnerInfoService.getPartnerInfoByBno(payment.getPartnerBno()));
                System.out.println("payment = " + payment);
                model.addAttribute("payment", payment);
                model.addAttribute("partner", partnerInfoService.getPartnerInfoByBno(payment.getPartnerBno()));
                model.addAttribute("impMerchantCode", impMerchantCode);
                return "smash/payment/bill"; // 결제 페이지로 이동
            } else {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error"; // 에러 페이지로 이동
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "결제 정보 조회 중 오류가 발생했습니다.");
            return "error"; // 에러 페이지로 이동
        }
    }
    // 결제 목록 조회
    @GetMapping("/list")
    public String paymentDetail(Model model) {
        try {
            List<PaymentDTO> payments = paymentService.allFindPayments(); // 모든 결제 정보 조회 (이후, 사용자 기준으로 조회)
            if (payments != null) {
                model.addAttribute("payments", payments);
                model.addAttribute("now", java.time.LocalDateTime.now());
                return "smash/payment/list"; // 결제 목록 페이지로 이동
            } else {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error"; // 에러 페이지로 이동
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "결제 정보 조회 중 오류가 발생했습니다.");
            return "error"; // 에러 페이지로 이동
        }
    }
    // 결제 상세 조회
    @GetMapping("/detail/{paymentIdx}")
    public String paymentDetail(@PathVariable Integer paymentIdx, Model model) {
        try {
            PaymentDTO payment = paymentService.getPaymentByIdx(paymentIdx);
            if (payment != null) {
                model.addAttribute("payment", payment);
                return "smash/payment/detail"; // 결제 상세 페이지로 이동
            } else {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error"; // 에러 페이지로 이동
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "결제 정보 조회 중 오류가 발생했습니다.");
            return "error"; // 에러 페이지로 이동
        }
    }
  
    // 결제 취소(DB삭제) + 환불(아임포트 API 호출)
    @PostMapping("/cancel/{paymentIdx}")
    public ResponseEntity<?> cancelPayment(@PathVariable Integer paymentIdx) {
        try {
            Payment canceledPayment = paymentService.cancelPayment(paymentIdx, false, null); // 자동 취소 여부는 false로 설정 (수동 취소)
            return ResponseEntity.ok(paymentService.entityToDto(canceledPayment));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 취소 실패");
        }
    }

    // 결제 삭제(DB삭제)
    @PatchMapping("/close/{paymentIdx}")
    public ResponseEntity<?> closePayment(@PathVariable Integer paymentIdx) {
        try {
            paymentService.closePayment(paymentIdx);
            return ResponseEntity.ok("결제 취소에 실패했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 정보 취소 실패");
        }
    }

    // 결제 전 : 결제서 DB 저장
    @PostMapping("/init")
    public ResponseEntity<String> saveEstimatePayment(@RequestBody PaymentDTO dto) {
        // System.out.println("PaymentDTO = " + dto);

        // 견적서 결제 저장
        PaymentDTO savedPayment = paymentService.savePayment(
            dto.getMemberEmail(),
            dto.getPartnerBno(),
            dto.getEstimateIdx(),
            dto.getSuggestedPrice()
        );
        // 견적서 idx보내기 
        return ResponseEntity.ok(savedPayment.getIdx().toString());
        
    }

    // 아임포트 결제 검증 및 저장 (+ iamUid, merchantUid, 결제완료 시각)
    @GetMapping("/verify/{impUid}")
    public ResponseEntity<?> verifyIamportPayment(
        @PathVariable("impUid") String impUid,
        @RequestParam String payType,
        @RequestParam String memberEmail,
        @RequestParam String partnerBno,
        @RequestParam Integer estimateIdx
        ) {
            System.out.println("impUid = " + impUid + ", payType = " + payType + ", memberEmail = " + memberEmail + ", partnerBno = " + partnerBno + ", estimateIdx = " + estimateIdx);
        try {
            com.siot.IamportRestClient.response.Payment impPayment = iamportClient.paymentByImpUid(impUid).getResponse();

            if (impPayment != null && "paid".equals(impPayment.getStatus())) {
                Payment savedPayment = paymentService.savePaymentFromIamport(impPayment, estimateIdx, payType, impUid);
                // impUid로 결제 정보(idx) 찾기
                Integer paymentIdx = savedPayment.getIdx();

                return ResponseEntity.ok(paymentIdx);
            } else {
                return ResponseEntity.badRequest().body("결제 상태가 유효하지 않음");
            }

        } catch (IamportResponseException | IOException e) {
            paymentService.refundPayment(impUid); // 결제 실패 시 결제 취소
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 조회 실패");
        } catch (Exception e) {
            paymentService.refundPayment(impUid); // 결제 실패 시 결제 취소
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 중 오류 발생");
        }
    }

}
