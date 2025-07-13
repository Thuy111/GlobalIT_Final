package com.bob.smash.repository;

import com.bob.smash.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Integer> {
  boolean existsByPath(String path);
}