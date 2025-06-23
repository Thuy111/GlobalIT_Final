package com.bob.smash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.ReviewDTO;

public interface ReviewService {
  Integer registerReview(ReviewDTO reviewDTO);
   List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx);
   ReviewDTO getReviewById(Integer reviewIdx);
void updateReview(ReviewDTO reviewDTO, List<MultipartFile> imageFiles);
void deleteReview(Integer reviewIdx, String currentUserEmail);


}
