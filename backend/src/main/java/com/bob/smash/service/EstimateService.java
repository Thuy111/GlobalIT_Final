package com.bob.smash.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.bob.smash.dto.EstimateDTO;

public interface EstimateService {
  // 의뢰서 사용 날짜 검색(등록시 필요)
  public LocalDateTime getUseDateByRequestIdx(Integer requestIdx);
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
  // 목록(사업자 번호로 필터링)
  List<EstimateDTO> getListByPartnerBno(String partnerBno);

  // 조회
  EstimateDTO get(Integer idx);
  // 조회(이미지 포함)
  EstimateDTO getWithImage(Integer idx);

  // 수정
  Integer modify(EstimateDTO dto);
  // 수정(이미지 포함)
  Integer modifyWithImage(EstimateDTO dto, 
                          List<Integer> deleteImageIdxList, 
                          List<MultipartFile> newImageFiles);

  // 낙찰 현황 수정
  Integer changeSelectStatus(EstimateDTO dto);
  // 의뢰서에 해당하는 견적서 전체 자동 미낙찰
  void autoSelect(Integer requestIdx);

  // 반납 현황 수정
  Integer changeReturnStatus(EstimateDTO dto);
  
  // 삭제
  void delete(Integer idx);
  // 삭제(이미지 포함)
  void deleteWithImage(Integer idx);
  // 견적서 일괄 삭제(회원탈퇴용)
  void deleteByPartnerBno(String bno);
}