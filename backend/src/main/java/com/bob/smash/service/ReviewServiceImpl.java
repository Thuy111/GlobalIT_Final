package com.bob.smash.service;

import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Review;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final EstimateRepository estimateRepository;

    @Override
    public void registerReview(ReviewDTO reviewDTO) {
        Estimate estimate = estimateRepository.findById(reviewDTO.getEstimateIdx())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 견적서"));

        Member member = memberRepository.findById(reviewDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        Review review = Review.builder()
                .estimate(estimate)
                .member(member)
                .star(reviewDTO.getStar())
                .comment(reviewDTO.getComment())
                .isModify((byte) 0)
                .createdAt(reviewDTO.getCreatedAt())
                .build();

        reviewRepository.save(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx) {
        List<Review> reviewList = reviewRepository.findByEstimate_Idx(estimateIdx);

        return reviewList.stream()
                .map(review -> ReviewDTO.builder()
                        .idx(review.getIdx())
                        .estimateIdx(review.getEstimate().getIdx())
                        .memberId(review.getMember().getEmailId())
                        .star(review.getStar())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .isModify(review.getIsModify())
                        .build())
                .collect(Collectors.toList());
    }
}
