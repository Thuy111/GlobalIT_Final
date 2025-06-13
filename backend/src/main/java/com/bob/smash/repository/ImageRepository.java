package com.bob.smash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bob.smash.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {
  // 이미지 이름 중복 검사시 활용할 메서드 추가 필요
}