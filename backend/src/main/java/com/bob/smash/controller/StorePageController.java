package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.dto.StorePageDTO;
import com.bob.smash.dto.StoreUpdateRequestDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.ReviewService;
import com.bob.smash.service.StorePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/smash/store")
@RequiredArgsConstructor
public class StorePageController {
    private final StorePageService storeService;
    private final EstimateService estimateService;
    private final ReviewService reviewService;

    @GetMapping("/{code}")
    public StorePageDTO getStore(@PathVariable String code,
                                 @RequestParam String memberId) {
        return storeService.getStorePage(code, memberId);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStore(@ModelAttribute StoreUpdateRequestDTO dto) {
        storeService.updateStore(dto);
        return ResponseEntity.ok("수정 완료");
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getReviewsByPartnerBno(@RequestParam("bno") String bno) {
        List<ReviewDTO> reviews = reviewService.getReviewsByPartnerBno(bno);
        double avgScore = reviewService.getAverageStarByPartnerBno(bno);

        return ResponseEntity.ok(Map.of(
            "reviews", reviews,
            "avgScore", avgScore
        ));
    }

    @GetMapping("/estimates")
    public ResponseEntity<?> getEstimatesByPartnerBno(@RequestParam("bno") String bno) {
        List<EstimateDTO> estimates = estimateService.getListByPartnerBno(bno);
        return ResponseEntity.ok(estimates);
    }
}