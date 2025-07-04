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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final ImageService imageService;
    private final EstimateService estimateService;

    @GetMapping("/")
    public String review() {
        return "redirect:/smash/review/list";
    }
    
    // 리뷰 목록
            @GetMapping("/list")
            public String list(Model model, HttpSession session) {
                if (!model.containsAttribute("reviewList")) {
                    model.addAttribute("reviewList", List.of()); // 빈 리스트라도 넣기
                }
                CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
                model.addAttribute("currentUser", currentUser);  // 템플릿에서 쓰려면 필수
                model.addAttribute("title", "리뷰 목록");
                return "smash/review/list";  // 명확한 뷰명 리턴 권장
            }


    // 업체가 쓴 리뷰 목록
    @GetMapping("/partnerlist")
    public String partnerList(@RequestParam("bno") String partnerBno, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("reviewList", reviewService.getReviewsByPartnerBno(partnerBno));
        return "redirect:/smash/review/list";
    }
    // 내가 쓴 리뷰 목록
    @GetMapping("/mylist")
    public String myList(HttpSession session, RedirectAttributes redirectAttributes) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/smash/";
        } else {
            if (currentUser.getRole() == 1) {
                return "redirect:/smash/review/partnerlist?bno=" + currentUser.getBno();
            } else {
                redirectAttributes.addFlashAttribute("reviewList", reviewService.getReviewsByMemberId(currentUser.getEmailId()));
            }
        }
        return "redirect:/smash/review/list";
    }


    // 전체 리뷰 목록
    @GetMapping("/all")
    public String allList(HttpSession session, RedirectAttributes redirectAttributes) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            // currentUser가 null이면 홈으로 리다이렉트
            return "redirect:/smash";
        } else {
            if(currentUser.getRole() == 2) {
                redirectAttributes.addFlashAttribute("reviewList", reviewService.getAllReviews());
            } else {
                return "redirect:/smash/review/mylist";
            }
        }
        return "redirect:/smash/review/list";
    }

    // 리뷰 등록 폼
        @GetMapping("/register")
        public String showReviewRegisterForm(@RequestParam("estimateIdx") Integer estimateIdx,
                                            HttpSession session,
                                            Model model) {
            CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
            if (currentUser == null) {
                return "redirect:/smash/"; // 또는 로그인 페이지 경로로 리다이렉트
            }

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

        // ✅ 관리자도 접근 가능하게 수정
        if (!reviewDTO.getMemberId().equals(currentUser.getEmailId()) && currentUser.getRole() != 2) {
            throw new IllegalArgumentException("본인 또는 관리자만 수정할 수 있습니다.");
        }

        // 본인인 경우만 1회 제한 적용
        if (reviewDTO.getIsModify() == 1 &&
            reviewDTO.getMemberId().equals(currentUser.getEmailId()) &&
            currentUser.getRole() != 2) {
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
                            @RequestParam(value = "from", required = false) String from,
                            HttpSession session) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        reviewService.updateReview(
            reviewDTO,
            imageFiles,
            "true".equals(isImageReset),
            currentUser.getEmailId(),
            currentUser.getRole()
        );

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
            if (currentUser == null || currentUser.getEmailId() == null) {
                throw new IllegalStateException("로그인이 필요합니다.");
            }

            // 서비스에 email과 role 둘 다 넘김
            reviewService.deleteReview(reviewIdx, currentUser.getEmailId(), currentUser.getRole());

            // ⭐ 보정 코드 추가
            if (!"mylist".equals(from) && requestIdx == null && estimateIdx != null) {
                requestIdx = estimateService.get(estimateIdx).getRequestIdx();
            }

            if ("mylist".equals(from)) {
                return "redirect:/smash/review/mylist";
            }

            return "redirect:/smash/request/detail/" + requestIdx;
        }

}