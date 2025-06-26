package com.bob.smash.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ImageDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.repository.EstimateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {
  private final EstimateRepository repository;
  private final ImageService imageService;

  // 등록
  @Override
  @Transactional
  public Integer register(EstimateDTO dto) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    return estimate.getIdx();
  }
  // 등록: 견적서 저장 + 이미지 저장 및 매핑
  @Override
  @Transactional
  public Integer registerWithImage(EstimateDTO dto, List<MultipartFile> imageFiles) {
    Estimate estimate = dtoToEntity(dto);
    repository.save(estimate);
    // 이미지 첨부가 있다면 이미지 등록 + 매핑
    if (imageFiles != null && !imageFiles.isEmpty()) {
      imageService.uploadAndMapImages("estimate", estimate.getIdx(), imageFiles);
    }
    return estimate.getIdx();
  }

  // 목록
  @Override
  public List<EstimateDTO> getList() {
    List<Estimate> result = repository.findAll();
    return result.stream().map(estimate -> entityToDto(estimate)).toList();
  }
  // 목록: 견적서 리스트 반환 (이미지 포함하고 싶으면 각 DTO에 이미지 세팅)
  @Override
  public List<EstimateDTO> getListWithImage() {
    List<Estimate> result = repository.findAll();
    List<Integer> idxList = result.stream().map(Estimate::getIdx).toList();
    // 한 번에 모든 견적서의 이미지 목록을 조회
    Map<Integer, List<ImageDTO>> imageMap = imageService.getImagesMapByTargets("estimate", idxList);
    return result.stream()
        .map(estimate -> {
            EstimateDTO dto = entityToDto(estimate);
            dto.setImages(imageMap.getOrDefault(estimate.getIdx(), List.of()));
            return dto;
        }).toList();

  }
  // 목록: 의뢰서 번호로 필터링 (이미지 포함)
  @Override
  public List<EstimateDTO> getListByRequestIdx(Integer requestIdx) {
    // 의뢰서 번호로 견적서 필터링
    List<Estimate> result = repository.findByRequest_Idx(requestIdx);
    List<Integer> idxList = result.stream().map(Estimate::getIdx).toList();
    // 견적서 이미지 한 번에 조회
    Map<Integer, List<ImageDTO>> imageMap = imageService.getImagesMapByTargets("estimate", idxList);
    // DTO에 이미지 세팅해서 반환
    return result.stream()
        .map(estimate -> {
            EstimateDTO dto = entityToDto(estimate);
            dto.setImages(imageMap.getOrDefault(estimate.getIdx(), List.of()));
            return dto;
        }).toList();
  }
  
  // 조회
  @Override
  public EstimateDTO get(Integer idx) {
    Estimate estimate = repository.findById(idx)
                                  .orElseThrow(() -> new IllegalArgumentException(idx+"번 견적서를 찾을 수 없습니다."));
    return entityToDto(estimate);
  }
  // 조회: 견적서 + 첨부 이미지 목록까지 DTO로 반환
  @Override
  public EstimateDTO getWithImage(Integer idx) {
    Estimate estimate = repository.findById(idx)
      .orElseThrow(() -> new IllegalArgumentException(idx + "번 견적서를 찾을 수 없습니다."));
    EstimateDTO dto = entityToDto(estimate);
    // 이미지 목록 조회 및 DTO에 세팅
    List<ImageDTO> images = imageService.getImagesByTarget("estimate", idx);
    dto.setImages(images);
    return dto;
  }

  // 수정
  @Override
  @Transactional
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
  // 수정: 견적서 수정 + 이미지 매핑 수정
  @Override
  @Transactional
  public Integer modifyWithImage(EstimateDTO dto, 
                                 List<Integer> deleteImageIdxList, 
                                 List<MultipartFile> newImageFiles) {
    // 견적서 수정
    Estimate estimate = repository.findById(dto.getIdx())
                                  .orElseThrow(() -> new IllegalArgumentException(dto.getIdx() + "번 견적서를 찾을 수 없습니다."));
    estimate.changeTitle(dto.getTitle());
    estimate.changeContent(dto.getContent());
    estimate.changePrice(dto.getPrice());
    estimate.changeIsDelivery(dto.getIsDelivery() ? (byte) 1 : (byte) 0);
    estimate.changeIsPickup(dto.getIsPickup() ? (byte) 1 : (byte) 0);
    estimate.changeReturnDate(dto.getReturnDate());
    estimate.changeModifiedAt(dto.getModifiedAt());
    repository.save(estimate);
    // 이미지 수정(삭제 + 추가 통합) 처리
    imageService.updateImagesByTarget("estimate", dto.getIdx(), deleteImageIdxList, newImageFiles);
    return estimate.getIdx();
  }

  // 낙찰 현황 수정
  @Override
  @Transactional
  public Integer selectStatus(EstimateDTO dto) {
    Estimate estimate = repository.findById(dto.getIdx())
                                  .orElseThrow(() -> new IllegalArgumentException(dto.getIdx() + "번 견적서를 찾을 수 없습니다."));
    // 낙찰된 견적서 상태 2로 변경
    estimate.changeIsSelected((byte) 2);
    repository.save(estimate);
    // 같은 의뢰서(requestIdx)에 속한 다른 견적서들 상태 1로 변경
    List<Estimate> otherEstimates = repository.findByRequest_Idx(dto.getRequestIdx());
    for (Estimate other : otherEstimates) {
        // 자기 자신은 제외
        if (!other.getIdx().equals(dto.getIdx())) {
          other.changeIsSelected((byte) 1);
          repository.save(other);
        }
    }
    return estimate.getIdx();
  }

  // 반납 현황 수정
  @Override
  @Transactional
  public Integer returnStatus(EstimateDTO dto) {
    Estimate estimate = repository.findById(dto.getIdx())
                                  .orElseThrow(() -> new IllegalArgumentException(dto.getIdx() + "번 견적서를 찾을 수 없습니다."));
    estimate.changeIsReturn(Boolean.TRUE.equals(dto.getIsReturn()) ? (byte) 1 : (byte) 0);
    repository.save(estimate);
    return estimate.getIdx();
  }

  // 삭제
  @Override
  @Transactional
  public void delete(Integer idx) {
    repository.deleteById(idx);
  }
  // 삭제: 견적서 + 첨부 이미지 목록 전체 삭제
  @Override
  @Transactional
  public void deleteWithImage(Integer idx) {
    Estimate estimate = repository.findById(idx)
      .orElseThrow(() -> new IllegalArgumentException(idx + "번 견적서를 찾을 수 없습니다."));
    // 첨부 이미지 삭제
    imageService.deleteImagesByTarget("estimate", idx);
    // 견적서 삭제
    repository.delete(estimate);
  }
  // 삭제: 사업자번호에 해당하는 모든 견적서 일괄 삭제
  @Override
  @Transactional
  public void deleteByPartnerBno(String bno) {
    // 사업자번호에 해당하는 모든 견적서 조회
    List<Estimate> estimateList = repository.findByPartnerInfo_Bno(bno);
    for (Estimate estimate : estimateList) {
        // 견적서+첨부이미지 모두 삭제
        deleteWithImage(estimate.getIdx());
        // (필요 시)리뷰 등 다른 연관 데이터도 여기서 삭제 가능
    }
    repository.deleteByPartnerInfo_Bno(bno); // 사업자번호로 견적 정보 전체 삭제
  }
}