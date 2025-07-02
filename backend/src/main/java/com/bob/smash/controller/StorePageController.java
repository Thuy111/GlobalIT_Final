package com.bob.smash.controller;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ReviewDTO;
import com.bob.smash.dto.StorePageDTO;
import com.bob.smash.dto.StoreUpdateRequestDTO;
import com.bob.smash.service.EstimateService;
import com.bob.smash.service.ReviewService;
import com.bob.smash.service.StorePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/smash/store")
@RequiredArgsConstructor
public class StorePageController {
    private final StorePageService storeService;
    private final EstimateService estimateService;
    private final ReviewService reviewService;

    // 상점 페이지 조회 (상점 코드 및 로그인한 사용자 ID로 조회)
    @GetMapping("/{code}")
    public StorePageDTO getStore(@PathVariable String code,
                                 @RequestParam String memberId) {
        return storeService.getStorePage(code, memberId);
    }

    // 상점 정보 업데이트 (이미지 업로드와 함께 정보 수정)
    @PutMapping("/update")
    public ResponseEntity<?> updateStore(@RequestParam String bno,  // 업체 bno
                                         @RequestParam String name,
                                         @RequestParam String tel,
                                         @RequestParam String region,
                                         @RequestParam String description,
                                         @RequestParam(required = false) List<Integer> deleteImageIds, // 삭제할 이미지들
                                         @RequestParam(required = false) List<MultipartFile> newImages, // 새로 업로드할 이미지들
                                         @RequestParam(required = false) List<StoreUpdateRequestDTO.ImageOrder> imageOrders) { // 이미지 순서 및 대표 이미지 설정

        StoreUpdateRequestDTO dto = new StoreUpdateRequestDTO();
        dto.setBno(bno);
        dto.setName(name);
        dto.setTel(tel);
        dto.setRegion(region);
        dto.setDescription(description);
        dto.setDeleteImageIds(deleteImageIds);
        dto.setNewImages(newImages);
        dto.setImageOrders(imageOrders);

        storeService.updateStore(dto);

        return ResponseEntity.ok("수정 완료");
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getReviewsByPartnerBno(@RequestParam("bno") String bno) {
        List<ReviewDTO> reviews = reviewService.getReviewsByPartnerBno(bno);
        double avgScore = reviewService.getAverageStarByPartnerBno(bno);
        int reviewCount = reviewService.countReviewsByPartnerBno(bno);

        return ResponseEntity.ok(Map.of(
            "reviews", reviews,
            "avgScore", avgScore,
            "count", reviewCount
        ));
    }

    @GetMapping("/estimates")
    public ResponseEntity<?> getEstimatesByPartnerBno(@RequestParam("bno") String bno) {
        List<EstimateDTO> estimates = estimateService.getListByPartnerBno(bno);
        return ResponseEntity.ok(estimates);
    }
}