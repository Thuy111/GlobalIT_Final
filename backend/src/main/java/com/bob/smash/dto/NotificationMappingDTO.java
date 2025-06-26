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
public class NotificationMappingDTO {
  private NotificationDTO notification;
  private String memberId;
  private Boolean isRead;
  private LocalDateTime readAt;
}