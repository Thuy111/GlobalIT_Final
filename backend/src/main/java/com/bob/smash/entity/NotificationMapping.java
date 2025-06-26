package com.bob.smash.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@IdClass(NotificationMapping.PK.class)
public class NotificationMapping {
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "noti_idx", nullable = false)
  private Notification notification;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
  private Byte isRead = 0; // 0: 읽지 않음, 1: 읽음;
  
  @Column(name = "read_at")
  private LocalDateTime readAt;

  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PK implements Serializable {
    private Integer notification;
    private String member;
  }
  
  public void changeIsRead(Byte isRead) {
    this.isRead = isRead;
    this.readAt = (isRead == 1) ? LocalDateTime.now() : null;
  }
}