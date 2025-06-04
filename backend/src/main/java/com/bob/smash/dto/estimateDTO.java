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
  private Integer idx;
  private Integer requestIdx;
  private String partnerBno;
  private String title;
  private String content;
  private Integer price;
  private LocalDateTime createdAt;
  private LocalDateTime returnDate;
  private Boolean isSelected;
  private Boolean isReturn;
}