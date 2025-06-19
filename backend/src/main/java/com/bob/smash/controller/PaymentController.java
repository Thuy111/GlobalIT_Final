package com.bob.smash.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bob.smash.service.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse; // 사용중 > 바로 사용하고 있으므로 사용처리로 안보임

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/payment")
public class PaymentController {
  // private final PaymentService paymentService;

  // Iamport API 클라이언트 설정
  // Iamport API 키와 시크릿 키는 application.properties 파일에서 관리
  private IamportClient iamportClient;

  @Value("${imp.api.key}")
  private String apiKey;

  @Value("${imp.api.secretkey}")
  private String secretKey;

  @PostConstruct
  public void init() {
      this.iamportClient = new IamportClient(apiKey, secretKey);
  }

  @GetMapping("/verify/{impUid}")
  public ResponseEntity<?> verifyIamportPayment(@PathVariable("impUid") String impUid) {
      try {
          // 아이엠포트 서버에서 결제 정보 조회
          com.siot.IamportRestClient.response.Payment impPayment = iamportClient.paymentByImpUid(impUid).getResponse();

          // 검증 로직 예: 금액, 상태 등 비교 가능
          if (impPayment != null && "paid".equals(impPayment.getStatus())) {
              return ResponseEntity.ok(impPayment);
          } else {
              return ResponseEntity.badRequest().body("결제 상태가 유효하지 않음");
          }

      } catch (IamportResponseException | IOException e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 조회 실패");
      }
  }

}
