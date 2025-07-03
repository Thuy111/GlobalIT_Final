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
public class NotificationDTO {
  private Integer idx;
  private String notice;
  private LocalDateTime createdAt;
  // Target 정보(의뢰서/견적서/리뷰 등)
  private String targetType;
  private Integer targetIdx;
  // 알림을 받는 회원 목록
  private List<String> memberIdList;
}