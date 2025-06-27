package com.bob.smash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.ReviewDTO;

public interface ReviewService {
  Integer registerReview(ReviewDTO reviewDTO);
   List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx);
   ReviewDTO getReviewById(Integer reviewIdx);
void updateReview(ReviewDTO reviewDTO, List<MultipartFile> imageFiles,boolean isImageReset);
void deleteReview(Integer reviewIdx, String currentUserEmail);
List<ReviewDTO> getReviewsByMemberId(String memberId);

//평균별점
double getAverageStarByEstimateIdx(Integer estimateIdx);


}
