package com.bob.smash.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateDTO {
  // 견적서 정보
  private Integer idx;
  private String title;
  private String content;
  private Boolean isDelivery;
  private Boolean isPickup;
  private Integer price;
  private LocalDateTime returnDate;
  private Byte isSelected;
  private Boolean isReturn;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  // 의뢰서 정보
  private Integer requestIdx;
  private LocalDateTime useDate;
  // 파트너 업체 정보
  private String partnerBno;
  private String partnerName;
  private String partnerTel;
  private String partnerRegion;
  private String partnerCode;
  private String partnerEmail;
  private String partnerNickname;
  // 첨부 이미지 정보
  private List<ImageDTO> images;
}