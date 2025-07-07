package com.bob.smash.service;


import com.bob.smash.dto.RequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public interface RequestService {
    // 등록 (이미지, hashtags 포함)
    Integer register(RequestDTO dto, List<MultipartFile> imageFiles);

    // 상세 페이지 보기 (detail 페이지용)
    RequestDTO get(Integer idx);

    // 목록 조회
    List<RequestDTO> getList();
    // 목록 조회(내가 작성한 의뢰서)
    List<RequestDTO> getListByMemberId(String memberId);

    // 삭제 (의뢰서 삭제하면 견적서도 삭제됨)
    void delete(Integer idx);

    // 수정
    void modify(RequestDTO dto,List<MultipartFile> newImages,List<Integer> deleteImageIds);

    // 낙찰현황(isDone) 변경
    Integer changeIsDone(Integer idx,Integer estimateIdx, String memberEmail,String partnerBno,Integer price);

    // 대여 현황(isGet) 변경
    Integer changeIsGet(Integer Idx);

    // ⭐ 무한스크롤용 페이지네이션 메서드 추가
    Map<String, Object> getPagedRequestList(int page, int size,String search,boolean hideExpired);

    // 의뢰서 관련 전체 삭제 : 이메일 (회원탈퇴용)
    void allDeleteByEmail(String email);

     // ✋ 낙찰된 업체 조회
    Optional<Long> findWinnerBnoByRequestIdx(Integer requestIdx);

    // 견적서 최저가
    Integer getMinEstimatePrice(Integer requestIdx);
}