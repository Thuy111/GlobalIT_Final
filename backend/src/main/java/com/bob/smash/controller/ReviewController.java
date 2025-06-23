package com.bob.smash.controller;

import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/smash/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/register")
    public String showReviewRegisterForm(@RequestParam("estimateIdx") Integer estimateIdx, Model model) {
        model.addAttribute("estimateIdx", estimateIdx);
        return "smash/reviewPage/register";
    }

    @PostMapping("/register")
    public String registerReview(@ModelAttribute ReviewDTO reviewDTO) {
      reviewDTO.setCreatedAt(LocalDateTime.now());
        reviewService.registerReview(reviewDTO);
        return "redirect:/smash/review/list?estimateIdx=" + reviewDTO.getEstimateIdx();
    }

    @GetMapping("/list")
    public String showReviewList(@RequestParam(value = "estimateIdx", required = false) Integer estimateIdx,
                                 Model model) {

        List<ReviewDTO> reviewList = (estimateIdx != null)
                ? reviewService.getReviewsByEstimateIdx(estimateIdx)
                : List.of();

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("estimateIdx", estimateIdx);
        return "smash/reviewPage/list";
    }
}
