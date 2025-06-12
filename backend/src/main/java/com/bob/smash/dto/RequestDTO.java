package com.bob.smash.dto;

import lombok.*;

import java.time.LocalDateTime;
// import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    private Integer idx;
   private String title;            // 제목
    private String content;          // 내용
    private LocalDateTime useDate;       // 대여 날짜
    // private String useRegion;        // 대여 지역 (주소 API로 가져오는 메인 주소)
    // private String detailAddress;    // 나머지 주소
    

    // private String hashtagLine;      
    // private MultipartFile[] images;  // 이미지 다중 업로드
}
