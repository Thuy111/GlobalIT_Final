package com.bob.smash.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.io.File;
import java.nio.file.Paths;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  // 🛠️ 파일 저장/파일 삭제 코드 중복으로 리팩토링 필요(의뢰서/견적서/리뷰 다 끝나고 여유 있을 때 확인)

  // (등록-단건)이미지 + 매핑 동시
  @Override
  @Transactional
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
  @Transactional
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
  @Transactional(readOnly = true)
  public List<ImageDTO> getImagesByTarget(String targetType, Integer targetIdx) {
    // 매핑 테이블에서 해당 targetType/targetIdx에 매핑된 모든 이미지매핑 조회
    List<ImageMapping> mappings = imageMappingRepository.findByTargetTypeAndTargetIdx(
      ImageMapping.TargetType.valueOf(targetType.toLowerCase()), targetIdx);
    // 각 매핑에서 연결된 Image와 매핑 정보를 DTO로 변환
    return mappings.stream()
    .map(mapping -> entityToDto(mapping.getImage(), mapping))
    .collect(Collectors.toList());
  }
  // (목록)여러 게시글별 이미지 조회
  @Override
  @Transactional(readOnly = true)
  public Map<Integer, List<ImageDTO>> getImagesMapByTargets(String targetType, List<Integer> targetIdxList) {
    // 매핑 테이블에서 해당 targetType/targetIdxList에 매핑된 모든 이미지매핑 조회
    List<ImageMapping> mappings = imageMappingRepository.findAllWithImageByTargetTypeAndTargetIdxIn(
      ImageMapping.TargetType.valueOf(targetType.toLowerCase()), targetIdxList);
    // 매핑을 targetIdx별로 그룹화하여 Map으로 변환
    return mappings.stream()
      .collect(Collectors.groupingBy(mapping -> mapping.getTargetIdx(),
        Collectors.mapping(mapping -> entityToDto(mapping.getImage(), mapping), Collectors.toList())));
  }

  // (수정-단건)게시글에서 특정 이미지 교체
  @Override
  @Transactional
  public ImageDTO updateImageOfTarget(String targetType, Integer targetIdx, Integer imageIdx, MultipartFile newFile) {
    // 매핑 및 이미지 찾기
    ImageMapping mapping = imageMappingRepository.findByImage(imageRepository.getReferenceById(imageIdx));
    if (mapping == null) {
      throw new IllegalArgumentException("해당 이미지 매핑이 존재하지 않습니다.");
    }
    Image image = mapping.getImage();
    if (image == null) {
      throw new IllegalArgumentException("해당 이미지 엔티티가 존재하지 않습니다.");
    }
    // === 파일이 없으면: 기존 이미지 + 매핑 삭제 ===
    if (newFile == null || newFile.isEmpty()) {
      // 기존 파일 삭제
      String oldFilePath = Paths.get(System.getProperty("user.dir"), "uploads", image.getPath()).toString();
      File oldFile = new File(oldFilePath);
      if (oldFile.exists()) oldFile.delete();
      // 매핑, 이미지 DB에서 삭제
      imageMappingRepository.delete(mapping);
      imageRepository.delete(image);
      // 반환 타입에 따라 null 반환 또는 예외, 혹은 삭제된 상태의 DTO 반환 (여기선 null 반환)
        return null;
    }
    // === 새 파일이 있는 경우 기존 파일 교체 ===
    // 새 파일 유효성 검사
    if (!validateImage(newFile)) {
      throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
    }
    // 기존 파일 삭제
    String oldFilePath = Paths.get(System.getProperty("user.dir"), "uploads", image.getPath()).toString();
    File oldFile = new File(oldFilePath);
    if (oldFile.exists()) oldFile.delete();
    // 새 파일 저장
    String uploadDir = System.getProperty("user.dir") + "/uploads/" + LocalDate.now();
    File dir = new File(uploadDir);
    if (!dir.exists()) dir.mkdirs();
    // 원본 파일명과 UUID를 조합해 저장 파일명 생성(중복방지)
    String originalFilename = newFile.getOriginalFilename();
    String uuid = UUID.randomUUID().toString();
    String saveName = uuid + "_" + originalFilename;
    File dest = new File(dir, saveName);
    try {
      newFile.transferTo(dest);
    } catch (Exception e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
    // Image 엔티티 정보 갱신 및 저장
    // (path는 uploads/날짜/uuid_파일명 형태로 저장)
    String relativePath = LocalDate.now() + "/" + saveName;
    image.changePath(relativePath);
    image.changeSName(saveName);
    image.changeOName(originalFilename);
    image.changeSize(newFile.getSize());
    image.changeType(newFile.getContentType());
    imageRepository.save(image);
    // DTO 변환 후 결과 반환
    return entityToDto(image, mapping);
  }
  // (수정-다중)게시글에서 여러 이미지 교체
  @Override
  @Transactional
  public List<ImageDTO> updateImagesOfTarget(String targetType, Integer targetIdx, Map<Integer, MultipartFile> updateMap) {
    List<ImageDTO> result = new ArrayList<>();
    for (Map.Entry<Integer, MultipartFile> entry : updateMap.entrySet()) {
      Integer imageIdx = entry.getKey();
      MultipartFile newFile = entry.getValue();
      // 각 이미지에 대해 단건 수정 메서드 호출 후 list에 추가
      result.add(updateImageOfTarget(targetType, targetIdx, imageIdx, newFile));
    }
    return result;
  }

  // (삭제-단건)게시글에서 특정 이미지 + 매핑 동시 
  @Override
  @Transactional
  public void deleteImageFromTarget(String targetType, Integer targetIdx, Integer imageIdx) {
    Image image = imageRepository.findById(imageIdx)
        .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
    ImageMapping mapping = imageMappingRepository.findByImage(image);
    if (mapping == null) {
        throw new IllegalArgumentException("해당 이미지 매핑이 존재하지 않습니다.");
    }
    //물리 파일 삭제 (upload 폴더에 있는 이미지)
    String filePath = Paths.get(System.getProperty("user.dir"), "uploads", image.getPath())
                           .toString();
    File file = new File(filePath);
    if (file.exists()) {
        boolean deleted = file.delete();
        System.out.println("삭제 성공 여부: " + deleted);
    } else {
        System.out.println("파일이 존재하지 않음");
    }
    // 매핑, 이미지 DB에서 삭제
    imageMappingRepository.delete(mapping);
    imageRepository.delete(image); 
  }
  // (삭제-다중)게시글에서 여러 이미지 + 매핑 동시
  @Override
  @Transactional
  public void deleteImagesFromTarget(String targetType, Integer targetIdx, List<Integer> imageIdxList) {
    for (Integer imageIdx : imageIdxList) {
      deleteImageFromTarget(targetType, targetIdx, imageIdx);
    }
  }
  // (삭제-전체)게시글 삭제 시/게시글 이미지 전체 삭제 시
  @Override
  @Transactional
  public void deleteImagesByTarget(String targetType, Integer targetIdx) {
    List<ImageMapping> mappings = imageMappingRepository.findByTargetTypeAndTargetIdx(
      ImageMapping.TargetType.valueOf(targetType.toLowerCase()), targetIdx
    );
    for (ImageMapping mapping : mappings) {
      Integer imageIdx = mapping.getImage().getIdx();
      deleteImageFromTarget(targetType, targetIdx, imageIdx);
    }
  }

  // 이미지 이름 중복 검사(🚧추후 구현 필요)
  @Override
  public boolean okImageName(String sName) {
    throw new UnsupportedOperationException("Unimplemented method 'okImageName'");
  }
  // 이미지 유효성 검사
  @Override
  public boolean validateImage(MultipartFile file) {
    // 파일이 null이거나 비어있으면 유효하지 않음
    if (file == null) {
        System.out.println("file is null");
        return false;
    }
    System.out.println("file.isEmpty: " + file.isEmpty());
    if (file.isEmpty()) {
        System.out.println("file is empty");
        return false;
    }
    // 허용할 이미지 타입
    String contentType = file.getContentType();
    System.out.println("contentType: " + contentType);
    if (contentType == null) return false;
    // 이미지 확장자/타입 체크
    System.out.println("확장자/타입 체크: " + contentType.startsWith("image/"));
    if (!contentType.startsWith("image/")) return false;
    // 확장자 직접 체크하고 싶으면 아래도 가능
    String filename = file.getOriginalFilename();
    System.out.println("filename: " + filename);
    if (filename != null && !filename.matches("(?i).*\\.(jpg|jpeg|png|gif)$")) return false;
    // 크기 제한 (예: 30MB)
    long maxSize = 10 * 1024 * 1024;
    System.out.println("file size: " + file.getSize());
    if (file.getSize() > maxSize) {
        System.out.println("file size too big");
        return false;
    }
    return true;
  }
  // 미사용/임시 이미지 삭제
  @Override
  @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행
  // @Scheduled(cron = "0 0 3 * * *") // 새벽 3시마다 실행
  public void deleteUnusedImages() {
    // uploads 폴더 내 전체 파일 목록 가져오기
    File uploadsDir = new File(System.getProperty("user.dir") + "/uploads");
    if (!uploadsDir.exists() || !uploadsDir.isDirectory()) return;
    File[] dateDirs = uploadsDir.listFiles(File::isDirectory);
    if (dateDirs == null) return;
    for (File dateDir : dateDirs) {
      File[] files = dateDir.listFiles();
      if (files == null) continue;
      for (File file : files) {
        // DB에 등록되어 있는 이미지인지 확인
        String relativePath = "/" + dateDir.getName() + "/" + file.getName();
        boolean used = imageRepository.existsByPath(relativePath);
        // DB에 없으면(미사용) 삭제
        if (!used) {
          boolean deleted = file.delete();
          if (deleted) System.out.println("미사용 이미지 삭제: " + file.getAbsolutePath());
          else         System.out.println("이미지 삭제 실패: " + file.getAbsolutePath());
        }
      }
    }
  }
}