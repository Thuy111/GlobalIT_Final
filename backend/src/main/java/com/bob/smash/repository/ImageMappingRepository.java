package com.bob.smash.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bob.smash.entity.Image;
import com.bob.smash.entity.ImageMapping;
import com.bob.smash.entity.ImageMapping.PK;
import com.bob.smash.entity.ImageMapping.TargetType;

public interface ImageMappingRepository extends JpaRepository<ImageMapping, PK> {
  // 특정 이미지 조회
  ImageMapping findByImage(Image image);
  // 하나의 의뢰서/견적서/리뷰에 매핑된 이미지 조회
  @Query("SELECT im FROM ImageMapping im JOIN FETCH im.image WHERE im.targetType = :targetType AND im.targetIdx = :targetIdx")
  List<ImageMapping> findByTargetTypeAndTargetIdx(TargetType targetType, Integer targetIdx);
  // 여러개의 의뢰서/견적서/리뷰에 매핑된 이미지 조회
  @Query("SELECT im FROM ImageMapping im JOIN FETCH im.image WHERE im.targetType = :targetType AND im.targetIdx IN :targetIdxList")
  List<ImageMapping> findAllWithImageByTargetTypeAndTargetIdxIn(
    @Param("targetType") TargetType targetType,
    @Param("targetIdxList") List<Integer> targetIdxList
  );
  // 특정 의뢰서/견적서/리뷰에 매핑된 기록 삭제(의뢰서/견적서/리뷰 삭제 시 활용)
  void deleteByTargetTypeAndTargetIdx(TargetType targetType, Integer targetIdx);
  // 특정 이미지에 매핑된 기록 삭제(이미지 삭제 시 활용)
  void deleteByImage(Image image);
}