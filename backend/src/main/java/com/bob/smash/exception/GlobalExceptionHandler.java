package com.bob.smash.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Global 예외 처리 클래스 (자동으로 모든 컨트롤러에서 발생하는 예외를 처리)
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateMemberException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateMember(DuplicateMemberException e) {
      return ResponseEntity
              .status(HttpStatus.CONFLICT)
              .body(Map.of("error", e.getMessage()));
  }

// 내장 예외 처리 
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
}
}
