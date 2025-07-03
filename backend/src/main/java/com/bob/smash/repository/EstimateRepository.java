package com.bob.smash.repository;

import com.bob.smash.entity.Estimate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstimateRepository extends JpaRepository<Estimate, Integer> {
  // 사업자번호로 견적서 전체 조회(내가 작성한 견적서 조회용)
  List<Estimate> findByPartnerInfo_Bno(String bno);
  // 회원 ID로 견적서 전체 조회(내가 받은 견적서 조회용)
  List<Estimate> findByRequest_IdxIn(List<Integer> requestIdxList);
  // 의뢰서에 해당하는 견적서 전체 조회(알림 생성용)
  List<Estimate> findByRequest_Idx(Integer requestIdx);
  // 낙찰 여부에 따라 의뢰서에 해당하는 견적서 전체 조회(알림 생성용)
  List<Estimate> findByRequest_IdxAndIsSelected(Integer requestIdx, Byte isSelected);
  // 낙찰 여부에 따라 의뢰서 목록에 해당하는 견적서 전체 조회(자동 미낙찰용)
  List<Estimate> findByRequest_IdxInAndIsSelected(List<Integer> requestIdxList, Byte isSelected);
  
  // 사업자번호에 해당하는 모든 견적 정보 삭제(회원 탈퇴 시)
  void deleteByPartnerInfo_Bno(String bno);
  // 의뢰서에 해당하는 모든 견적 정보 삭제(의뢰서 삭제 시)
  void deleteByRequest_Idx(Integer requestIdx);

  // 🤚 낙찰 없체 조회( user의 상세 주소 보임 용)
  @Query("SELECT e.partnerInfo.bno FROM Estimate e WHERE e.request.idx = :requestIdx AND e.isSelected = 2")
  Optional<Long> findWinnerBnoByRequestIdx(@Param("requestIdx") Integer requestIdx);
}