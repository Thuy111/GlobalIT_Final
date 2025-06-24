package com.bob.smash.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.Request;

public interface EstimateService {
  // 등록
  Integer register(EstimateDTO dto);
  // 등록(이미지 포함)
  Integer registerWithImage(EstimateDTO dto, List<MultipartFile> imageFiles);
  // 목록
  List<EstimateDTO> getList();
  // 목록(이미지 포함)
  List<EstimateDTO> getListWithImage();
  // 목록(의뢰서 번호로 필터링)
  List<EstimateDTO> getListByRequestIdx(Integer requestIdx);
  // 조회
  EstimateDTO get(Integer idx);
  // 조회(이미지 포함)
  EstimateDTO getWithImage(Integer idx);
  // 수정
  Integer modify(EstimateDTO dto);
  // 수정(이미지 포함)
  Integer modifyWithImage(EstimateDTO dto, List<Integer> deleteImageIdxList, List<MultipartFile> newImageFiles);
  // 반납 현황 수정
  Integer returnStatus(EstimateDTO dto);
  // 삭제
  void delete(Integer idx);
  // 삭제(이미지 포함)
  void deleteWithImage(Integer idx);
  // 견적서 일괄 삭제(회원탈퇴용)
  void deleteByPartnerBno(String bno);

  // dto -> entity
  default Estimate dtoToEntity(EstimateDTO dto) {
    Estimate estimate = Estimate.builder()
                                .idx(dto.getIdx())
                                .request(Request.builder().idx(dto.getRequestIdx()).build())
                                .partnerInfo(PartnerInfo.builder().bno(dto.getPartnerBno()).build())
                                .title(dto.getTitle())
                                .content(dto.getContent())
                                .price(dto.getPrice())
                                .returnDate(dto.getReturnDate())
                                .isDelivery(Boolean.TRUE.equals(dto.getIsDelivery()) ? (byte) 1 : (byte) 0)
                                .isPickup(Boolean.TRUE.equals(dto.getIsPickup()) ? (byte) 1 : (byte) 0)
                                .isSelected(dto.getIsSelected())
                                .isReturn(Boolean.TRUE.equals(dto.getIsReturn()) ? (byte) 1 : (byte) 0)
                                .createdAt(dto.getCreatedAt())
                                .modifiedAt(dto.getModifiedAt())
                                .build();
    return estimate;
  }
  // entity -> dto
  default EstimateDTO entityToDto(Estimate estimate) {
    EstimateDTO dto = EstimateDTO.builder()
                                 .idx(estimate.getIdx())
                                 .title(estimate.getTitle())
                                 .content(estimate.getContent())
                                 .price(estimate.getPrice())
                                 .returnDate(estimate.getReturnDate())
                                 .isDelivery(estimate.getIsDelivery() == 1)
                                 .isPickup(estimate.getIsPickup() == 1)
                                 .isSelected(estimate.getIsSelected())
                                 .isReturn(estimate.getIsReturn() == 1)
                                 .createdAt(estimate.getCreatedAt())
                                 .modifiedAt(estimate.getModifiedAt())
                                 .requestIdx(estimate.getRequest().getIdx())
                                 .partnerBno(estimate.getPartnerInfo().getBno())
                                 .partnerName(estimate.getPartnerInfo().getName())
                                 .partnerTel(estimate.getPartnerInfo().getTel())
                                 .partnerRegion(estimate.getPartnerInfo().getRegion())
                                 .build();
    return dto;
  }
}