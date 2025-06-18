package com.bob.smash.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ImageDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.repository.EstimateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {
  private final ImageService imageService;
  private final EstimateRepository repository;

  // 등록
  @Override
  public Integer register(EstimateDTO dto) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    return estimate.getIdx();
  }

  // 목록
  @Override
  public List<EstimateDTO> getList() {
    List<Estimate> result = repository.findAll();
    return result.stream().map(estimate -> entityToDto(estimate)).toList();
  }

  // 반납 현황 수정
  @Override
  public Integer returnStatus(EstimateDTO dto) {
    Estimate estimate = repository.findById(dto.getIdx())
                                  .orElseThrow(() -> new IllegalArgumentException(dto.getIdx() + "번 견적서를 찾을 수 없습니다."));
    estimate.changeIsReturn(Boolean.TRUE.equals(dto.getIsReturn()) ? (byte) 1 : (byte) 0);
    repository.save(estimate);
    return estimate.getIdx();
  }
  
  // 조회
  @Override
  public EstimateDTO get(Integer idx) {
    Estimate estimate = repository.findById(idx)
                                  .orElseThrow(() -> new IllegalArgumentException(idx+"번 견적서를 찾을 수 없습니다."));
    return entityToDto(estimate);
  }

  // 수정
  @Override
  public Integer modify(EstimateDTO dto) {
    Estimate estimate = repository.getReferenceById(dto.getIdx());
    estimate.changeTitle(dto.getTitle());
    estimate.changeContent(dto.getContent());
    estimate.changePrice(dto.getPrice());
    estimate.changeIsDelivery(dto.getIsDelivery() ? (byte) 1 : (byte) 0);
    estimate.changeIsPickup(dto.getIsPickup() ? (byte) 1 : (byte) 0);
    estimate.changeReturnDate(dto.getReturnDate());
    estimate.changeModifiedAt(dto.getModifiedAt());
    repository.save(estimate);
    return estimate.getIdx();
  }

  // 삭제
  @Override
  public void delete(Integer idx) {
    repository.deleteById(idx);
  }

  // 등록: 견적서 저장 + 이미지 저장 및 매핑
  public Integer registerWithImage(EstimateDTO dto, List<MultipartFile> imageFiles) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    // 이미지 첨부가 있다면 이미지 등록 + 매핑
    if (imageFiles != null && !imageFiles.isEmpty()) {
      imageService.uploadAndMapImages("estimate", estimate.getIdx(), imageFiles);
    }
    return estimate.getIdx();
  }

  // 목록: 견적서 리스트 반환 (이미지 포함하고 싶으면 각 DTO에 이미지 세팅)
  public List<EstimateDTO> getListWithImage() {
    List<Estimate> result = repository.findAll();
    return result.stream()
      .map(estimate -> {
        EstimateDTO dto = entityToDto(estimate);
        // 각 견적서에 해당하는 이미지 목록 조회 및 세팅
        List<ImageDTO> images = imageService.getImagesByTarget("estimate", estimate.getIdx());
        dto.setImages(images);
        return dto;
      })
      .toList();
  }
  
  // 조회: 견적서 + 첨부 이미지 목록까지 DTO로 반환
  public EstimateDTO getWithImage(Integer idx) {
    Estimate estimate = repository.findById(idx)
      .orElseThrow(() -> new IllegalArgumentException(idx + "번 견적서를 찾을 수 없습니다."));
    EstimateDTO dto = entityToDto(estimate);
    // 이미지 목록 조회 및 DTO에 세팅
    List<ImageDTO> images = imageService.getImagesByTarget("estimate", idx);
    dto.setImages(images);
    return dto;
  }

  // 삭제: 견적서 + 첨부 이미지 목록 전체 삭제
  public void deleteWithImage(Integer idx) {
    Estimate estimate = repository.findById(idx)
      .orElseThrow(() -> new IllegalArgumentException(idx + "번 견적서를 찾을 수 없습니다."));
    // 첨부 이미지 삭제
    imageService.deleteImagesByTarget("estimate", idx);
    // 견적서 삭제
    repository.delete(estimate);
  }
}