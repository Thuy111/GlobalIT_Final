package com.bob.smash.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bob.smash.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
  void deleteByMember_EmailId(String emailId); // 회원 이메일로 리뷰 전체 삭제
  void deleteByEstimate_Idx(Integer idx); // 견적서 번호로 리뷰 전체 삭제
}
