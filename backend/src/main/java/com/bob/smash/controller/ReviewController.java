package com.bob.smash.controller;

import com.bob.smash.dto.CurrentUserDTO;
import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.ImageService;
import com.bob.smash.service.ReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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
        return "smash/review/register";
    }

    // 리뷰 등록 처리
    @PostMapping("/register")
    public String registerReview(@ModelAttribute ReviewDTO reviewDTO,
                                 @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                                 HttpSession session) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        reviewDTO.setMemberId(currentUser.getEmailId());
        reviewDTO.setCreatedAt(LocalDateTime.now());
        Integer reviewIdx = reviewService.registerReview(reviewDTO);
        if (imageFiles != null && imageFiles.stream().anyMatch(file -> !file.isEmpty())) {
            imageService.uploadAndMapImages("review", reviewIdx, imageFiles);
        }
        Integer estimateIdx = reviewDTO.getEstimateIdx();
        EstimateDTO estimateDTO = estimateService.get(estimateIdx);
        Integer requestIdx = estimateDTO.getRequestIdx();
        return "redirect:/smash/request/detail/" + requestIdx;             
    }

    // 리뷰 목록
    @GetMapping("/list")
    public String showReviewList(Model model) {
        model.addAttribute("title", "리뷰 목록");
        return "smash/review/list";
    }

    // 리뷰 수정 폼
    @GetMapping("/update")
    public String showUpdateForm(@RequestParam("reviewIdx") Integer reviewIdx,
                                 @RequestParam(value = "from", required = false) String from,
                                 HttpSession session,
                                 Model model) {
        ReviewDTO reviewDTO = reviewService.getReviewById(reviewIdx);
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (!reviewDTO.getMemberId().equals(currentUser.getEmailId())) {
            throw new IllegalArgumentException("본인 리뷰만 수정할 수 있습니다.");
        }
        if (reviewDTO.getIsModify() == 1) {
            throw new IllegalStateException("이미 수정한 리뷰입니다.");
        }
        model.addAttribute("review", reviewDTO);
        model.addAttribute("from", from); // ⭐ 추가
        model.addAttribute("title", "리뷰 수정");
        return "smash/review/update";
    }

    // 리뷰 수정 처리
    @PostMapping("/update")
    public String updateReview(@ModelAttribute ReviewDTO reviewDTO,
                               @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                               @RequestParam(value = "isImageReset", required = false) String isImageReset,
                               @RequestParam(value = "from", required = false) String from) {
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
    public String deleteReview(@RequestParam("reviewIdx") Integer reviewIdx,
                              @RequestParam(value = "estimateIdx", required = false) Integer estimateIdx,
                              @RequestParam(value = "requestIdx", required = false) Integer requestIdx,
                              @RequestParam(value = "from", required = false) String from,
                              HttpSession session) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (currentUser.getEmailId() == null) {
            throw new IllegalStateException("로그인 이메일 정보를 불러올 수 없습니다.");
        }
        reviewService.deleteReview(reviewIdx, currentUser.getEmailId());

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
        String currentUserId = currentUserDTO.getEmailId();
        // 내 리뷰만 조회
        List<ReviewDTO> myReviewList = reviewService.getReviewsByMemberId(currentUserId);
        model.addAttribute("reviewList", myReviewList);
        model.addAttribute("isMyList", true); // 내 리뷰 전용 표시
        model.addAttribute("title", "내가 쓴 리뷰 목록");
        return "smash/review/list";
    }
}