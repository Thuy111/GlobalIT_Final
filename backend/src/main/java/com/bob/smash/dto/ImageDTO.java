package com.bob.smash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {
  private Integer imageIdx;
  private String sName;
  private String oName;
  private String path;
  private String type;
  private Long size;
  // 매핑 정보
  private String targetType; // "request", "estimate", "review"
  private Integer targetIdx; // target Type에 해당하는 의뢰서/견적서/리뷰의 idx
}