package com.bob.smash.dto;

import lombok.*;

import java.time.LocalDateTime;
// import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.Image;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    private Integer idx;
    private String title;            // 제목
    private String content;          // 내용
    private LocalDateTime useDate;       // 대여 날짜

    private LocalDateTime createdAt; //의뢰서 생성날짜
    private List<Hashtag> hashtagList;
    private String hashtags; //해시태그 입력 (예: "운동 캠핑 자전거")

     
    // private List<MultipartFile> imageFiles; //업로드된 이미지들 (많은 사진 업로드)
    // private List<Image> imageList; // 저장된 이미지 정보를 응답으로 줄 때 사용


    // private String useRegion;        // 대여 지역 (주소 API로 가져오는 메인 주소)
    // private String detailAddress;    // 나머지 주소
    

       
    
}
