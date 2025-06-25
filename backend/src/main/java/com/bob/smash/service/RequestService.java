package com.bob.smash.service;


import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;



import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;


@Service
public interface RequestService {

    // 등록 (이미지, hashtags 포함)
    Integer register(RequestDTO dto, Member member,List<MultipartFile> imageFiles);

    // 상세 페이지 보기 (detail 페이지용)
    RequestDTO get(Integer idx);


    // 목록 조회////////(test 용, 추후 삭제 필요)
    List<RequestDTO> getList();

    // 삭제 (의뢰서 삭제하면 견적서도 삭제됨)
    void delete(Integer idx);

    // 수정
    void modify(RequestDTO dto,List<MultipartFile> newImages,List<Integer> deleteImageIds);

    // 낙찰현황(isDone) 변경
    void changeIsDone(Integer idx, Integer estimateIdx);

    // ⭐ 무한스크롤용 페이지네이션 메서드 추가
    Map<String, Object> getPagedRequestList(int page, int size,String search);

    // 의뢰서 관련 전체 삭제 : 이메일 (회원탈퇴용)
    void allDeleteByEmail(String email);


    // DTO → Entity 변환
    default Request dtoToEntity(RequestDTO dto, Member member) {
        return Request.builder()
                .idx(dto.getIdx())
                .title(dto.getTitle())
                .content(dto.getContent())
                .useDate(dto.getUseDate())                
                .useRegion(dto.getUseRegion()) 
                .isDone((byte) 0)
                .isGet((byte) 0)
                .isModify((byte) 0)
                .createdAt(LocalDateTime.now())
                .member(member)
                .build();
    }

    // Entity → DTO 변환
    default RequestDTO entityToDto(Request request,List<Hashtag> hashtags) {  //hashtag
        //hashtags 묶음
        String hashtagStr = hashtags.stream()
        .map(Hashtag::getTag)  
        .collect(Collectors.joining(" "));
        //주소
        String useRegion = request.getUseRegion();
        String mainAddress = "";
        String detailAddress = "";

        if (useRegion != null) {
            int spaceIndex = useRegion.indexOf(" ");
            if (spaceIndex > 0) {
                mainAddress = useRegion.substring(0, spaceIndex);
                detailAddress = useRegion.substring(spaceIndex + 1);
            } else {
                mainAddress = useRegion;
            }
        }

        return RequestDTO.builder()
            .idx(request.getIdx())
            .title(request.getTitle())
            .content(request.getContent())
            .useDate(request.getUseDate())
            .createdAt(request.getCreatedAt())
            .useRegion(mainAddress)         // 메인 주소
            .detailAddress(detailAddress)  // 나머지 주소
            .hashtags(hashtagStr)  //묶음 Hashtag 추가
            .hashtagList(hashtags)  //Hashtag 추가  
            .isModify(request.getIsModify())
            .isDone(request.getIsDone())
            .writerEmail(request.getMember().getEmailId())      
            .build();
    }
}
