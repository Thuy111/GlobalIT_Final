package com.bob.smash.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.bob.smash.entity.Hashtag;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    private Integer idx;
    private String title;            // 제목
    private String content;          // 내용

    private LocalDateTime useDate;   // 대여 날짜

    private LocalDateTime createdAt; // 의뢰서 생성날짜

    // 낙찰 상태 (0: 대기, 1: 완료, 그 외: 실패)
    private Byte isDone;

    // D-Day 문자열 (예: "D-3")
    private String dDay;

    // 해시태그 (Entity 객체 목록)
    private List<Hashtag> hashtagList;

    // 해시태그 입력 문자열 (예: "운동 캠핑 자전거")
    private String hashtags;

    // 지역
    private String useRegion; // 대여 지역 (주소 API로 가져오는 메인 주소)
    private String detailAddress;

    // 이미지 정보
    private List<ImageDTO> images;

    //수정 여부
    private Byte isModify;

    // 대여 여부
    private Byte isGet;

    // 작성자 ID 확인
    private String writerEmail;

    
}
