package com.bob.smash.event;

import org.springframework.stereotype.Component;

import com.bob.smash.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlarmEventListener {
  private final NotificationService service;
}