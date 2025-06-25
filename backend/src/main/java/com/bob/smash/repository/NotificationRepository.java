package com.bob.smash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.smash.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}