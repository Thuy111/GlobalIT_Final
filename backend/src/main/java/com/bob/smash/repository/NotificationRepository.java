package com.bob.smash.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.bob.smash.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
  // (스케줄)알림 정리용
  @Query("SELECT n FROM Notification n WHERE NOT EXISTS (SELECT 1 FROM NotificationMapping m WHERE m.notification = n)")
  List<Notification> findOrphanedNotifications();
}