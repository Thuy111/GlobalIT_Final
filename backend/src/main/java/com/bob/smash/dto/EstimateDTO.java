package com.bob.smash.dto;

import java.time.LocalDateTime;

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
  private LocalDateTime createdAt;
  private Boolean isSelected;
  private Boolean isReturn;
  // 의뢰서 정보
  private Integer requestIdx;
  private String requestMemberId;
  // 파트너 업체 정보
  private String partnerBno;
  private String partnerName;
  private String partnerTel;
  private String partnerRegion;
}