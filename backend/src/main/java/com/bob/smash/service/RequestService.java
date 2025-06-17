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

    // 등록 (이미지 포함)
    Integer register(RequestDTO dto, Member member,List<MultipartFile> imageFiles);

    // 상세보기
    RequestDTO get(Integer idx);


    // 목록 조회
    List<RequestDTO> getList();

    // ⭐ 무한스크롤용 페이지네이션 메서드 추가
    Map<String, Object> getPagedRequestList(int page, int size,String search);

    // DTO → Entity 변환
    default Request dtoToEntity(RequestDTO dto, Member member) {
        return Request.builder()
                .idx(dto.getIdx())
                .title(dto.getTitle())
                .content(dto.getContent())
                .useDate(dto.getUseDate())
                // .useRegion("서울") // 예시값, 추후 DTO에 추가되면 수정
                .useRegion(dto.getUseRegion()) //  DTO에서 꺼냄
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
            .map(tag -> "#" + tag.getTag())
            .collect(Collectors.joining(" "));


        return RequestDTO.builder()
            .idx(request.getIdx())
            .title(request.getTitle())
            .content(request.getContent())
            .useDate(request.getUseDate())
            .createdAt(request.getCreatedAt())
            .useRegion(request.getUseRegion())
            .hashtags(hashtagStr)  //묶음 Hashtag 추가
            .hashtagList(hashtags)  //Hashtag 추가        
            .build();
    }
}
