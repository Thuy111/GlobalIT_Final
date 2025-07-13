package com.bob.smash.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.bob.smash.dto.ImageDTO;

public interface ImageService {
  // (등록-단건)게시글에서 이미지 + 매핑 동시
  ImageDTO uploadAndMapImage(String targetType, Integer targetIdx, MultipartFile file);
  // (등록-다중)게시글에서 여러 이미지 + 매핑 동시
  List<ImageDTO> uploadAndMapImages(String targetType, Integer targetIdx, List<MultipartFile> files);

  // (목록)게시글별 이미지 조회
  List<ImageDTO> getImagesByTarget(String targetType, Integer targetIdx);
  // (목록)여러 게시글별 이미지 조회
  Map<Integer, List<ImageDTO>> getImagesMapByTargets(String targetType, List<Integer> targetIdxList);

  // (삭제-전체)게시글 삭제 시/게시글 이미지 전체 삭제 시
  void deleteImagesByTarget(String targetType, Integer targetIdx);
  // (삭제-단건)게시글에서 특정 이미지 + 매핑 동시
  void deleteImageFromTarget(String targetType, Integer targetIdx, Integer imageIdx);
  // (삭제-다중)게시글에서 여러 이미지 + 매핑 동시
  void deleteImagesFromTarget(String targetType, Integer targetIdx, List<Integer> imageIdxList);
  
  // (수정)게시글에서 첨부 이미지 삭제 및 추가
  void updateImagesByTarget(String targetType, Integer targetIdx, List<Integer> deleteImageIdxList, List<MultipartFile> newImageFiles);
  
  // 미사용/임시 이미지 삭제
  void deleteUnusedImages();
  // 이미지 이름 중복 검사
  boolean okImageName(String sName);
  // 이미지 유효성 검사
  boolean validateImage(MultipartFile file);
}