package com.bob.smash.repository;

import com.bob.smash.entity.Estimate;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, Integer> {
  List<Estimate> findByPartnerInfo_Bno(String bno); // 사업자번호로 견적서 전체 조회
  List<Estimate> findByRequest_Idx(Integer requestIdx); // 의뢰서에 해당하는 견적서 전체 조회
  
  void deleteByPartnerInfo_Bno(String bno); // 사업자번호에 해당하는 모든 견적 정보 삭제
  void deleteByRequest_Idx(Integer requestIdx); // 의뢰서에 해당하는 모든 견적 정보 삭제
}