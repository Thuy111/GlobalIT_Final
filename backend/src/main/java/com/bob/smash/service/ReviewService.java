package com.bob.smash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.ReviewDTO;

public interface ReviewService {
  // 리뷰 등록
  Integer registerReview(ReviewDTO reviewDTO);

  // 리뷰 목록(전체)
  List<ReviewDTO> getAllReviews();
  // 리뷰 목록(견적서별)
  List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx);
  // 리뷰 목록(업체별)
  List<ReviewDTO> getReviewsByPartnerBno(String bno);

  // 리뷰 상세 조회
  ReviewDTO getReviewById(Integer reviewIdx);

  // 리뷰 수정
  void updateReview(ReviewDTO reviewDTO, List<MultipartFile> imageFiles, boolean isImageReset, String currentUserEmail, int currentUserRole);

  // 리뷰 삭제
  void deleteReview(Integer reviewIdx, String currentUserEmail, int currentUserRole);

  // 리뷰 작성자 조회
  List<ReviewDTO> getReviewsByMemberId(String memberId);

  //평균별점
  double getAverageStarByEstimateIdx(Integer estimateIdx);

  // 🤚 Review 작성 여부
  boolean hasUserReviewed(String emailId, Integer estimateIdx);

  // 업체별 평균 별점
  double getAverageStarByPartnerBno(String bno);
  
  // 업체 리뷰갯수
  int countReviewsByPartnerBno(String bno);
}