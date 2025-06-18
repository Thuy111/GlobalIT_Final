package com.bob.smash.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
  void deleteByPartnerInfo_Bno(String bno); // 결제 정보 삭제
}
