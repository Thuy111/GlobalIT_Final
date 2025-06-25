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
public class NotificationDTO {
  private Integer idx;
  private String notice;
  private LocalDateTime createdAt;
  private Boolean isRead; // 0: unread, 1: read
  private LocalDateTime readAt; // 읽은 시간
  // Target 정보(의뢰서/견적서/리뷰 등)
  private String targetType; // request, estimate, review
  private Integer targetIdx;
  // 회원 정보
  private String memberId;
}