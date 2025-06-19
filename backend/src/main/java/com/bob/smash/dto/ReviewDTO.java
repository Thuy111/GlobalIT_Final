package com.bob.smash.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String nickname;
    private Byte star;
    private String comment;
    private LocalDate createdAt;
    private List<ImageDTO> images;
    private Integer estimateIdx;
}
