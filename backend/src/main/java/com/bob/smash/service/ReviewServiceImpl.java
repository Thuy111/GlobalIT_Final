package com.bob.smash.service;

import com.bob.smash.dto.ImageDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Review;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final EstimateRepository estimateRepository;
    private final ImageService imageService; // ✅ 추가

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

        return savedReview.getIdx();
    }

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
@Override
public void updateReview(ReviewDTO reviewDTO, List<MultipartFile> imageFiles) {
    Review review = reviewRepository.findById(reviewDTO.getIdx())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

    // 리뷰 내용 수정 (change 메서드 호출)
    review.changeStar(reviewDTO.getStar());
    review.changeComment(reviewDTO.getComment());
    review.changeIsModify((byte) 1);  // 수정 완료 표시

    reviewRepository.save(review);

    // 이미지 수정 (새로 받은 이미지가 있으면 처리)
if (imageFiles != null && imageFiles.stream().anyMatch(file -> !file.isEmpty())) {
    imageService.deleteImagesByTarget("review", review.getIdx());
    imageService.uploadAndMapImages("review", review.getIdx(), imageFiles);
}

}

//삭제
    @Override
    public void deleteReview(Integer reviewIdx, String currentUserEmail) {
        Review review = reviewRepository.findById(reviewIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 작성자 이메일과 로그인한 이메일 일치 여부 확인
        String reviewWriterEmail = review.getMember().getEmailId();
        if (!reviewWriterEmail.equals(currentUserEmail)) {
            throw new IllegalArgumentException("본인 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

                @Override
        public List<ReviewDTO> getReviewsByMemberId(String memberId) {
            List<Review> reviews = reviewRepository.findByMember_EmailId(memberId);
            return reviews.stream()
                    .map(this::convertToDTO)
                    .toList();
        }
            private ReviewDTO convertToDTO(Review review) {
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
        }

}
