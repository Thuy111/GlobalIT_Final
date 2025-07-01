package com.bob.smash.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePageDTO {
    private String bno; // 사업자 번호
    private String code; // 사업자 코드
    private String name; // 업체 명
    private String tel; // 업체 번호
    private String region; // 업체 주소
    private String description; // 업체 설명
    private boolean isOwner; // 업체의 오너인지 여부

    private List<String> imageURLs; // 이미지  URL 리스트
    private List<EstimateDTO> estimates; // 견적서 리스트
    private List<ReviewDTO> reviews; // 리뷰 리스트
    private double averageRating; // 별점 평균
}
