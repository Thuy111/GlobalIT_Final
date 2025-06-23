package com.bob.smash.service;

import java.util.List;

import com.bob.smash.dto.ReviewDTO;

public interface ReviewService {
  void registerReview(ReviewDTO reviewDTO);
   List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx);
}
