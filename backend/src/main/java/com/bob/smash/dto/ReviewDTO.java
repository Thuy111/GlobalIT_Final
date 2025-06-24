package com.bob.smash.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer idx;

    private Integer estimateIdx; // 연관된 견적서 ID

    private String memberId; // 리뷰 작성자 ID (현재 로그인한 사용자)

    private Byte star; // 별점 (1~5)

    private String comment; // 리뷰 내용

    private LocalDateTime createdAt;

    private Byte isModify;

    private List<ImageDTO> images;

    private String nickname;
}
