package com.bob.smash.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.bob.smash.dto.ImageDTO;
import com.bob.smash.entity.Image;
import com.bob.smash.entity.ImageMapping;

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
  // (수정-단건)게시글에서 특정 이미지 교체
  ImageDTO updateImageOfTarget(String targetType, Integer targetIdx, Integer imageIdx, MultipartFile newFile);
  // (수정-다중)게시글에서 여러 이미지 교체
  List<ImageDTO> updateImagesOfTarget(String targetType, Integer targetIdx, Map<Integer, MultipartFile> updateMap);
  
  // 미사용/임시 이미지 삭제
  void deleteUnusedImages();
  // 이미지 이름 중복 검사
  boolean okImageName(String sName);
  // 이미지 유효성 검사
  boolean validateImage(MultipartFile file);
  
  // Image entity 생성
  default Image toImageEntity(MultipartFile file, String uploadDir, String saveName, String originalFilename) {
    String webPath = "/" + LocalDate.now() + "/" + saveName;
    return Image.builder()
                .sName(saveName)
                .oName(originalFilename)
                .path(webPath)
                .type(file.getContentType())
                .size(file.getSize())
                .build();
  }
  // ImageMapping entity 생성
  default ImageMapping toImageMappingEntity(String targetType, Integer targetIdx, Image image) {
    return ImageMapping.builder()
                       .targetType(ImageMapping.TargetType.valueOf(targetType.toLowerCase()))
                       .targetIdx(targetIdx)
                       .image(image)
                       .build();
  }

  // dto → entity(Image)
  default Image dtoToEntity(ImageDTO dto) {
    return Image.builder()
                .idx(dto.getImageIdx())
                .sName(dto.getSName())
                .oName(dto.getOName())
                .path(dto.getPath())
                .type(dto.getType())
                .size(dto.getSize())
                .build();
  }
  // dto → entity(ImageMapping)
  default ImageMapping dtoToMappingEntity(ImageDTO dto) {
    return ImageMapping.builder()
                       .targetType(ImageMapping.TargetType.valueOf(dto.getTargetType().toLowerCase()))
                       .targetIdx(dto.getTargetIdx())
                       .image(Image.builder().idx(dto.getImageIdx()).build())
                       .build();
  }
  // entity → dto
  default ImageDTO entityToDto(Image image, ImageMapping mapping) {
    return ImageDTO.builder()
                   .imageIdx(image.getIdx())
                   .sName(image.getSName())
                   .oName(image.getOName())
                   .path(image.getPath())
                   .type(image.getType())
                   .size(image.getSize())
                   .targetType(mapping.getTargetType().name().toLowerCase()) // "request", "estimate", "review"
                   .targetIdx(mapping.getTargetIdx())
                   .build();
  }
}