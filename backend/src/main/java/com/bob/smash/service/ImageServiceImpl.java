package com.bob.smash.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.io.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.bob.smash.dto.ImageDTO;
import com.bob.smash.entity.Image;
import com.bob.smash.entity.ImageMapping;
import com.bob.smash.repository.ImageMappingRepository;
import com.bob.smash.repository.ImageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
  private final ImageRepository imageRepository;
  private final ImageMappingRepository imageMappingRepository;

  // (등록-단건)이미지 + 매핑 동시
  @Override
  public ImageDTO uploadAndMapImage(String targetType, Integer targetIdx, MultipartFile file) {
    // 이미지 유효성 검사
    if (!validateImage(file)) {
        throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
    }
    // 파일 저장 경로 생성(예: /uploads/날짜(2025-06-12)/uuid_파일명)
    String uploadDir = System.getProperty("user.dir") + "/uploads/" + LocalDate.now();
    // 원본 파일명과 UUID를 조합해 저장 파일명 생성(중복방지)
    String originalFilename = file.getOriginalFilename();
    String uuid = UUID.randomUUID().toString();
    String saveName = uuid + "_" + originalFilename;
    // 폴더가 없으면 생성
    File dir = new File(uploadDir);
    if (!dir.exists()) dir.mkdirs();
    // 실제 저장할 파일 객체 생성
    File dest = new File(dir, saveName);
    // 파일 저장
    try {
      file.transferTo(dest);
    } catch (Exception e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
    // Image 엔티티로 변환 및 DB 저장(default 메서드 활용)
    Image image = toImageEntity(file, uploadDir, saveName, originalFilename);
    image = imageRepository.save(image);
    // ImageMapping 엔티티로 변환 및 DB 저장(default 메서드 활용)
    ImageMapping mapping = toImageMappingEntity(targetType, targetIdx, image);
    mapping = imageMappingRepository.save(mapping);
    // DTO로 변환 후 반환
    return entityToDto(image, mapping);
  }
  // (등록-다중)여러 이미지 + 매핑 동시
  @Override
  public List<ImageDTO> uploadAndMapImages(String targetType, Integer targetIdx, List<MultipartFile> files) {
    List<ImageDTO> result = new ArrayList<>();
    for (MultipartFile file : files) {
      // 각 파일에 대해 단건 등록 메서드 호출 후 list에 추가
      result.add(uploadAndMapImage(targetType, targetIdx, file));
    }
    return result;
  }

  // (목록)게시글별 이미지 조회
  @Override
  public List<ImageDTO> getImagesByTarget(String targetType, Integer targetIdx) {
    // 매핑 테이블에서 해당 targetType/targetIdx에 매핑된 모든 이미지매핑 조회
    List<ImageMapping> mappings = imageMappingRepository.findByTargetTypeAndTargetIdx(
      ImageMapping.TargetType.valueOf(targetType.toLowerCase()), targetIdx);
    // 각 매핑에서 연결된 Image와 매핑 정보를 DTO로 변환
    return mappings.stream()
    .map(mapping -> entityToDto(mapping.getImage(), mapping))
    .collect(Collectors.toList());
  }

  // (수정-단건)게시글에서 특정 이미지 교체
  @Override
  public ImageDTO updateImageOfTarget(String targetType, Integer targetIdx, Integer imageIdx, MultipartFile newFile) {
    throw new UnsupportedOperationException("Unimplemented method 'updateImageOfTarget'");
  }
  // (수정-다중)게시글에서 여러 이미지 교체
  @Override
  public List<ImageDTO> updateImagesOfTarget(String targetType, Integer targetIdx, Map<Integer, MultipartFile> updateMap) {
    throw new UnsupportedOperationException("Unimplemented method 'updateImagesOfTarget'");
  }

  // (삭제-전체)게시글 삭제 시/게시글 이미지 전체 삭제 시
  @Override
  public void deleteImagesByTarget(String targetType, Integer targetIdx) {
    throw new UnsupportedOperationException("Unimplemented method 'deleteImagesByTarget'");
  }
  // (삭제-단건)게시글에서 특정 이미지 + 매핑 동시
  @Override
  public void deleteImageFromTarget(String targetType, Integer targetIdx, Integer imageIdx) {
    throw new UnsupportedOperationException("Unimplemented method 'deleteImageFromTarget'");
  }
  // (삭제-다중)게시글에서 여러 이미지 + 매핑 동시
  @Override
  public void deleteImagesFromTarget(String targetType, Integer targetIdx, List<Integer> imageIdxList) {
    throw new UnsupportedOperationException("Unimplemented method 'deleteImagesFromTarget'");
  }

  // 미사용/임시 이미지 삭제
  @Override
  public void deleteUnusedImages() {
    throw new UnsupportedOperationException("Unimplemented method 'deleteUnusedImages'");
  }
  // 이미지 이름 중복 검사
  @Override
  public boolean okImageName(String sName) {
    throw new UnsupportedOperationException("Unimplemented method 'okImageName'");
  }
  // 이미지 유효성 검사
  @Override
  public boolean validateImage(MultipartFile file) {
    // 파일이 null이거나 비어있으면 유효하지 않음
    if (file == null || file.isEmpty()) return false;
    // 허용할 이미지 타입
    String contentType = file.getContentType();
    if (contentType == null) return false;
    
    // 이미지 확장자/타입 체크 (jpg, jpeg, png, gif만 허용 예시)
    if (!contentType.startsWith("image/")) return false;
    // 확장자 직접 체크하고 싶으면 아래도 가능 (선택)
    String filename = file.getOriginalFilename();
    if (filename != null && !filename.matches(".*\\.(jpg|jpeg|png|gif)$"))
    return false;
    
    // (선택) 크기 제한 (예: 10MB)
    long maxSize = 10 * 1024 * 1024;
    if (file.getSize() > maxSize) return false;
    return true;
  }
}