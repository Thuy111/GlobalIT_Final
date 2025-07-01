package com.bob.smash.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class StoreUpdateRequestDTO {
    private String name;
    private String tel;
    private String region;
    private String description;
    private String bno; // 업체 bno로 식별

    private List<MultipartFile> newImages; // 새로 업로드할 이미지들
    private List<Integer> deleteImageIds; // 삭제할 이미지 ID 목록 (Long -> Integer로 변경)
}
