package com.bob.smash.controller;

import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/smash/review")
public class ReviewRestController {

    private final ReviewService reviewService;

    @GetMapping("/bno")
    public ResponseEntity<?> getReviewsByPartnerBno(@RequestParam("bno") String bno) {
        List<ReviewDTO> reviews = reviewService.getReviewsByPartnerBno(bno);
        double avgScore = reviewService.getAverageStarByPartnerBno(bno);

        return ResponseEntity.ok(Map.of(
            "reviews", reviews,
            "avgScore", avgScore
        ));
    }
}
