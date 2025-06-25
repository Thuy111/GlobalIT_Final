package com.bob.smash.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
  List<Payment> findByMember_EmailId(String memberEmail); // 회원별 결제 정보 조회
  List<Payment> findByEstimate_Idx(Integer estimateIdx); // 견적서에 해당하는 모든 결제 정보 조회

  Optional<Payment> findByImpUid(String impUid); // 아이엠포트 결제 고유 ID : 결제 정보 조회
  // 견적서 번호 : 결제 정보 조회
  @Query("SELECT p FROM Payment p WHERE p.estimate.idx = :estimateIdx AND p.status = com.bob.smash.entity.Payment.Status.ready")
  Optional<Payment> findReadyPaymentByEstimateIdx(@Param("estimateIdx") Integer estimateIdx);


  // 견적서에 해당하는 결제 정보의 idx 조회. 단, 결제 준비 중(ready)인 것만
  @Query("SELECT p.idx FROM Payment p WHERE p.estimate.idx = :estimateIdx AND p.status = com.bob.smash.entity.Payment.Status.ready")
  Integer findReadyIdxByEstimateIdx(@Param("estimateIdx") Integer estimateIdx);


  
  void deleteByPartnerInfo_Bno(String bno); // 사업자번호에 해당하는 모든 결제 정보 삭제
  void deleteByEstimate_Idx(Integer estimateIdx); // 견적서에 해당하는 모든 결제 정보 삭제
}
