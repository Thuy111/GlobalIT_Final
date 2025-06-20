package com.bob.smash.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
  Optional<Payment> findByEstimate_Idx(Integer estimateIdx); // 견적서에 해당하는 결제 정보 조회
  void deleteByPartnerInfo_Bno(String bno); // 사업자번호에 해당하는 모든 결제 정보 삭제
  void deleteByEstimate_Idx(Integer estimateIdx); // 견적서에 해당하는 모든 결제 정보 삭제
}
