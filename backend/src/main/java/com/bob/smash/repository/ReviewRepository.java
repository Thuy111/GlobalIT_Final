package com.bob.smash.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
  void deleteByMember_EmailId(String emailId); // 회원 이메일로 리뷰 전체 삭제
  void deleteByEstimate_Idx(Integer idx); // 견적서 번호로 리뷰 전체 삭제
  List<Review> findByEstimate_Idx(Integer estimateIdx); // 견적서 번호맞는 리뷰
  List<Review> findByMember_EmailId(String email);
  boolean existsByMember_EmailIdAndEstimate_Idx(String emailId, Integer estimateIdx); // 🤚 Review 작성 여부

  // 업체 BNO를 기준으로 리뷰 조회
@Query("""
    SELECT r FROM Review r
    JOIN Estimate e ON r.estimate.idx = e.idx
    WHERE e.partnerInfo.bno = :bno
""")
List<Review> findByPartnerBno(@Param("bno") String bno);

@Query("""
    SELECT AVG(r.star) FROM Review r
    JOIN Estimate e ON r.estimate.idx = e.idx
    WHERE e.partnerInfo.bno = :bno
""")
Double findAvgStarByPartnerBno(@Param("bno") String bno);

@Query("""
    SELECT COUNT(r) FROM Review r
    JOIN Estimate e ON r.estimate.idx = e.idx
    WHERE e.partnerInfo.bno = :bno
""")
int countByPartnerBno(@Param("bno") String bno);
}
