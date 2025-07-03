package com.bob.smash.service;

import com.bob.smash.dto.ImageDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Review;
import com.bob.smash.event.ReviewEvent;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final EstimateRepository estimateRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    // 리뷰 등록
    @Override
    public Integer registerReview(ReviewDTO reviewDTO) {
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
        Review savedReview = reviewRepository.save(review);
        // 리뷰 작성 이벤트 발행(알림 생성용)
        eventPublisher.publishEvent(new ReviewEvent(this, savedReview.getIdx(), ReviewEvent.Action.CREATED));
        return savedReview.getIdx();
    }

    // 리뷰 목록
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviewList = reviewRepository.findAll();
        return reviewList.stream()
                         .map(this::convertToDTO)
                         .collect(Collectors.toList());
    }
    // 리뷰 목록(견적서별)
    @Override
    public List<ReviewDTO> getReviewsByEstimateIdx(Integer estimateIdx) {
        List<Review> reviewList = reviewRepository.findByEstimate_Idx(estimateIdx);
        return reviewList.stream()
                         .map(review -> {
                            // ✅ imageService 주입 받아서 사용
                            List<ImageDTO> imageDTOs = imageService.getImagesByTarget("review", review.getIdx());
                            return ReviewDTO.builder()
                                            .idx(review.getIdx())
                                            .estimateIdx(review.getEstimate().getIdx())
                                            .memberId(review.getMember().getEmailId())
                                            .nickname(review.getMember().getNickname())
                                            .star(review.getStar())
                                            .comment(review.getComment())
                                            .createdAt(review.getCreatedAt())
                                            .isModify(review.getIsModify())
                                            .images(imageDTOs)
                                            .build();
                         })
                         .collect(Collectors.toList());
    }
    // 리뷰 목록(업체별)
    @Override
    public List<ReviewDTO> getReviewsByPartnerBno(String bno) {
        List<Review> reviewList = reviewRepository.findByPartnerBno(bno);
        return reviewList.stream()
                         .map(this::convertToDTO)
                         .collect(Collectors.toList());
    }

    // 리뷰 상세 조회
    @Override
    public ReviewDTO getReviewById(Integer reviewIdx) {
        Review review = reviewRepository.findById(reviewIdx)
                                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        List<ImageDTO> imageDTOs = imageService.getImagesByTarget("review", reviewIdx);
        return ReviewDTO.builder()
                        .idx(review.getIdx())
                        .estimateIdx(review.getEstimate().getIdx())
                        .memberId(review.getMember().getEmailId())
                        .nickname(review.getMember().getNickname())
                        .star(review.getStar())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .isModify(review.getIsModify())
                        .images(imageDTOs)
                        .build();
    }

    // 리뷰 수정
    @Override
    public void updateReview(ReviewDTO reviewDTO, List<MultipartFile> imageFiles, boolean isImageReset, String currentUserEmail, int currentUserRole) {
        Review review = reviewRepository.findById(reviewDTO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 본인 또는 관리자 확인
        boolean isOwner = review.getMember().getEmailId().equals(currentUserEmail);
        boolean isAdmin = currentUserRole == 2;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("본인 또는 관리자만 수정할 수 있습니다.");
        }

        // 관리자일 경우 isModify 갱신하지 않음
        if (isOwner) {
            review.changeIsModify((byte) 1);
        }

        review.changeStar(reviewDTO.getStar());
        review.changeComment(reviewDTO.getComment());

        reviewRepository.save(review);
            if (isImageReset) {
            // ✅ 이미지 초기화 요청이면 삭제만 수행
            imageService.deleteImagesByTarget("review", review.getIdx());
        }
        // 이미지 수정 (새로 받은 이미지가 있으면 처리)
        if (imageFiles != null && imageFiles.stream().anyMatch(file -> !file.isEmpty())) {
            imageService.deleteImagesByTarget("review", review.getIdx());
            imageService.uploadAndMapImages("review", review.getIdx(), imageFiles);
        }
        // 리뷰 수정 이벤트 발행(알림 생성용)
        eventPublisher.publishEvent(new ReviewEvent(this, review.getIdx(), ReviewEvent.Action.UPDATED));
    }

    // 삭제
            @Override
            public void deleteReview(Integer reviewIdx, String currentUserEmail, int currentUserRole) {
                Review review = reviewRepository.findById(reviewIdx)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
                if (currentUserRole != 2) { // 관리자가 아니면 본인 리뷰만 삭제 가능
                    if (!review.getMember().getEmailId().equals(currentUserEmail)) {
                        throw new IllegalArgumentException("본인 리뷰만 삭제할 수 있습니다.");
                    }
                }
                reviewRepository.delete(review);
            }



    // 리뷰 작성자 조회
    @Override
    public List<ReviewDTO> getReviewsByMemberId(String memberId) {
        List<Review> reviews = reviewRepository.findByMember_EmailId(memberId);
        return reviews.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // 리뷰 DTO 변환 메서드
    private ReviewDTO convertToDTO(Review review) {
        List<ImageDTO> imageDTOs = imageService.getImagesByTarget("review", review.getIdx());
        return ReviewDTO.builder()
                        .idx(review.getIdx())
                        .estimateIdx(review.getEstimate().getIdx())
                                .requestIdx(                      // ⭐ 반드시 추가
            review.getEstimate() != null &&
            review.getEstimate().getRequest() != null
                ? review.getEstimate().getRequest().getIdx()
                : null
        )
                        .memberId(review.getMember().getEmailId())
                        .nickname(review.getMember().getNickname())
                        .star(review.getStar())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .isModify(review.getIsModify())
                        .images(imageDTOs)
                        .build();
    }

    //리뷰평균점수
    @Override
    public double getAverageStarByEstimateIdx(Integer estimateIdx) {
        List<Review> reviews = reviewRepository.findByEstimate_Idx(estimateIdx);
        if (reviews.isEmpty()) return 0.0;

        return reviews.stream()
                .mapToDouble(r -> r.getStar())  // star는 Byte → double
                .average()
                .orElse(0.0);
    }

    // 🤚 Review 작성 여부
    @Override
    public boolean hasUserReviewed(String emailId, Integer estimateIdx) {
        return reviewRepository.existsByMember_EmailIdAndEstimate_Idx(emailId, estimateIdx);
    }

    //업체별 평균 별점
    @Override
    public double getAverageStarByPartnerBno(String bno) {
        Double avg = reviewRepository.findAvgStarByPartnerBno(bno);
        return avg != null ? avg : 0.0;
    }

    // 리뷰카운트
    @Override
    public int countReviewsByPartnerBno(String bno) {
    return reviewRepository.countByPartnerBno(bno);
}
}