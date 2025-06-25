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
  @ExceptionHandler(IllegalStateException.class) // 잘못된 상태
  public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }
  @ExceptionHandler(RuntimeException.class) // 런타임 예외
  public ResponseEntity<String> handleRuntime(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
  @ExceptionHandler(IllegalArgumentException.class) // 잘못된 인자
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }
  @ExceptionHandler(Exception.class) // 모든 예외 처리
  public ResponseEntity<String> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
  }

}
