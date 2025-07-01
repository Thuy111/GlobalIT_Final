package com.bob.smash.controller;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.ImageService;
import com.bob.smash.service.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final ImageService imageService;
    private final EstimateService estimateService;

    // 리뷰 등록 폼
    @GetMapping("/register")
    public String showReviewRegisterForm(@RequestParam("estimateIdx") Integer estimateIdx, Model model) {
        model.addAttribute("estimateIdx", estimateIdx);
        model.addAttribute("title", "리뷰 등록");
        return "smash/reviewPage/register";
    }

    // 리뷰 등록 처리
    @PostMapping("/register")
    public String registerReview(
            @ModelAttribute ReviewDTO reviewDTO,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @AuthenticationPrincipal OAuth2User oauth2User
    ) {
        String currentUserEmail = extractEmailFromOAuth2User(oauth2User);
        reviewDTO.setMemberId(currentUserEmail);
        reviewDTO.setCreatedAt(LocalDateTime.now());

        Integer reviewIdx = reviewService.registerReview(reviewDTO);

        if (imageFiles != null && imageFiles.stream().anyMatch(file -> !file.isEmpty())) {
            imageService.uploadAndMapImages("review", reviewIdx, imageFiles);
        }

         // ✅ Truy ngược từ estimateIdx → requestIdx để quay lại trang chi tiết
        Integer estimateIdx = reviewDTO.getEstimateIdx();
        EstimateDTO estimateDTO = estimateService.get(estimateIdx);
        Integer requestIdx = estimateDTO.getRequestIdx();

        return "redirect:/smash/request/detail/" + requestIdx;             
    }

    // 리뷰 목록
    @GetMapping("/list")
    public String showReviewList(
            @RequestParam(value = "estimateIdx", required = false) Integer estimateIdx,
            @AuthenticationPrincipal OAuth2User oauth2User,
            Model model
    ) {
        String currentUser = extractEmailFromOAuth2User(oauth2User);

        List<ReviewDTO> reviewList = (estimateIdx != null)
                ? reviewService.getReviewsByEstimateIdx(estimateIdx)
                : List.of();
       
        double avg = reviewService.getAverageStarByEstimateIdx(estimateIdx);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("estimateIdx", estimateIdx);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("avgScore", avg);
        model.addAttribute("title", "리뷰 목록");

        return "smash/reviewPage/list";
    }

// 리뷰 수정 폼
@GetMapping("/update")
public String showUpdateForm(
        @RequestParam("reviewIdx") Integer reviewIdx,
        @RequestParam(value = "from", required = false) String from, // ⭐ 추가
        @AuthenticationPrincipal OAuth2User oauth2User,
        Model model
) {
    ReviewDTO reviewDTO = reviewService.getReviewById(reviewIdx);
    String currentUser = extractEmailFromOAuth2User(oauth2User);

    if (!reviewDTO.getMemberId().equals(currentUser)) {
        throw new IllegalArgumentException("본인 리뷰만 수정할 수 있습니다.");
    }
    if (reviewDTO.getIsModify() == 1) {
        throw new IllegalStateException("이미 수정한 리뷰입니다.");
    }

    model.addAttribute("review", reviewDTO);
    model.addAttribute("from", from); // ⭐ 추가
    model.addAttribute("title", "리뷰 수정");
    return "smash/reviewPage/update";
}


// 리뷰 수정 처리
@PostMapping("/update")
public String updateReview(
        @ModelAttribute ReviewDTO reviewDTO,
        @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
        @RequestParam(value = "isImageReset", required = false) String isImageReset,
        @RequestParam(value = "from", required = false) String from, 
        @AuthenticationPrincipal OAuth2User oauth2User
) {
    reviewService.updateReview(reviewDTO, imageFiles, "true".equals(isImageReset));

    if ("mylist".equals(from)) {
        return "redirect:/smash/review/mylist";
    }

    if (reviewDTO.getRequestIdx() == null) {
        EstimateDTO estimateDTO = estimateService.get(reviewDTO.getEstimateIdx());
        return "redirect:/smash/request/detail/" + estimateDTO.getRequestIdx();
    }
    return "redirect:/smash/request/detail/" + reviewDTO.getRequestIdx();
}


// 리뷰 삭제
@GetMapping("/delete")
public String deleteReview(
        @RequestParam("reviewIdx") Integer reviewIdx,
        @RequestParam(value = "estimateIdx", required = false) Integer estimateIdx,
        @RequestParam(value = "requestIdx", required = false) Integer requestIdx,
        @RequestParam(value = "from", required = false) String from, // ⬅ 이미 받아오고 있음
        @AuthenticationPrincipal OAuth2User oauth2User
) {
    if (oauth2User == null) {
        throw new IllegalStateException("로그인이 필요합니다.");
    }

    String currentUser = extractEmailFromOAuth2User(oauth2User);
    if (currentUser == null) {
        throw new IllegalStateException("로그인 이메일 정보를 불러올 수 없습니다.");
    }

    reviewService.deleteReview(reviewIdx, currentUser);

    // ✅ 수정된 부분: from 파라미터 확인
    if ("mylist".equals(from)) {
        return "redirect:/smash/review/mylist";
    }

    return "redirect:/smash/request/detail/" + requestIdx;
}




// 내가 쓴 리뷰 목록
@GetMapping("/mylist")
public String showMyReviewList(HttpSession session, Model model) {
    // 세션에서 로그인 유저 정보 가져오기
    CurrentUserDTO currentUserDTO = (CurrentUserDTO) session.getAttribute("currentUser");

    if (currentUserDTO == null) {
        return "redirect:/smash/"; // 로그인 안 되어 있으면 로그인 페이지로 리디렉트
    }

    String currentUser = currentUserDTO.getEmailId();

    // 내 리뷰만 조회
    List<ReviewDTO> myReviewList = reviewService.getReviewsByMemberId(currentUser);

    model.addAttribute("reviewList", myReviewList);
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("isMyList", true); // 내 리뷰 전용 표시
    return "smash/reviewPage/list";
}

    // 공통 로그인 이메일 추출
    private String extractEmailFromOAuth2User(OAuth2User oauth2User) {
        if (oauth2User == null) return null;

        // 일반 OAuth2 (예: 구글)
        if (oauth2User.getAttribute("email") != null) {
            return oauth2User.getAttribute("email");
        }

        // 카카오
        Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
        if (kakaoAccount != null) {
            return (String) kakaoAccount.get("email");
        }

        return null;
    }
}
