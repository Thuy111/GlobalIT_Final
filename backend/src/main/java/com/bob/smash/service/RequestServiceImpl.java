package com.bob.smash.service;

import com.bob.smash.dto.ImageDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.HashtagMapping;

import com.bob.smash.entity.Image;
import com.bob.smash.entity.ImageMapping;
import com.bob.smash.entity.ImageMapping.TargetType;
import com.bob.smash.dto.RequestListDTO;

import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.HashtagMappingRepository;
import com.bob.smash.repository.HashtagRepository;
import com.bob.smash.repository.ImageMappingRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;

    // hashtag
    private final HashtagRepository hashtagRepository;
    private final HashtagMappingRepository hashtagMappingRepository;

    //사진
    private final ImageService imageService;   


    // 등록///////////////////////////////////////////////////
    @Override
    public Integer register(RequestDTO dto, Member member,List<MultipartFile> imageFiles) {
    Request entity = dtoToEntity(dto, member);
    Request saved = requestRepository.save(entity);

    // [1] 해시태그 처리
    if (dto.getHashtags() != null && !dto.getHashtags().trim().isEmpty()) {
        String[] tags = dto.getHashtags().trim().split("\\s+");

        for (String rawTag : tags) {
            String tag = rawTag.trim();

            if (!tag.isEmpty()) {
                // 존재하는 해시태그 있는지 확인
                Hashtag hashtag = hashtagRepository.findByTag(tag)
                        .orElseGet(() -> hashtagRepository.save(
                                Hashtag.builder().tag(tag).build()));

                // 매핑 생성 후 저장
                HashtagMapping mapping = HashtagMapping.builder()
                        .hashtag(hashtag)
                        .request(saved)
                        .build();

                hashtagMappingRepository.save(mapping);
            }
        }
    }

     // [2] 이미지 업로드 및 매핑
    if (imageFiles != null && !imageFiles.isEmpty()) {
        imageFiles.stream()
            .filter(file -> !file.isEmpty())
            .forEach(file -> {
                try {
                    imageService.uploadAndMapImage("request", saved.getIdx(), file);
                } catch (Exception e) {
                    log.error("이미지 업로드 실패: {}", e.getMessage());
                }
            });
    }
    
    return saved.getIdx();

    }

    // 상세 페이지 조회/////////////////////////////////////
    @Override
    public RequestDTO get(Integer idx) {
        Optional<Request> result = requestRepository.findById(idx);

        //** hashtag**********************/
        if (result.isPresent()) {
        Request request = result.get();

        // hashtagrepository에서 꺼냄
        List<Hashtag> hashtags = hashtagMappingRepository.findHashtagsByRequestIdx(idx);
        
       // Request DTO 기본 변환 (이미지 제외)
        RequestDTO dto = entityToDto(request, hashtags);

        // 이미지 리스트 조회 
        List<ImageDTO> images = imageService.getImagesByTarget("request", idx);

        // 이미지 리스트 DTO에 세팅
        dto.setImages(images);

        return dto;

        }    
        return null;
    }
    

    // 전체 목록 조회////////////////////////////////////
    @Override
    public List<RequestDTO> getList() {
        List<Request> list = requestRepository.findAll();        
        return list.stream().map(request -> {
        List<Hashtag> hashtags = hashtagMappingRepository.findHashtagsByRequestIdx(request.getIdx());
                return entityToDto(request, hashtags);
        }).collect(Collectors.toList());
    }

    // 무한스크롤용 페이지네이션 기능 구현
@Override
public Map<String, Object> getPagedRequestList(int page, int size, String search) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Request> requestPage;

    if (search != null && !search.isBlank()) {
        requestPage = requestRepository.findByTitleContaining(search, pageable);
    } else {
        requestPage = requestRepository.findAll(pageable);
    }

    List<RequestDTO> requestDTOs = requestPage.getContent()
            .stream()
            .map(request -> {
                // 해시태그 리스트 조회
                List<Hashtag> hashtagList = hashtagMappingRepository.findHashtagsByRequestIdx(request.getIdx());

                // 해시태그 리스트 -> 문자열 (공백 구분) 변환
                String hashtagsString = hashtagList.stream()
                        .map(Hashtag::getTag)
                        .collect(Collectors.joining(" "));
                // D-DAY
                String dDay = calculateDDay(request.getCreatedAt().toLocalDate(), request.getUseDate().toLocalDate());

                return RequestDTO.builder()
                        .idx(request.getIdx())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .createdAt(request.getCreatedAt())  // LocalDateTime 그대로
                        .useDate(request.getUseDate())
                        .isDone(request.getIsDone()) 
                        .hashtagList(hashtagList)
                        .hashtags(hashtagsString)
                        .dDay(dDay)
                        // useRegion과 images 정보가 필요하면 아래에 넣어야 함 (현재 정보 없음)
                        .build();
            })
            .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("request", requestDTOs);
    response.put("currentPage", page);
    response.put("totalPages", requestPage.getTotalPages());
    response.put("hasNext", requestPage.hasNext());

    // 전체 해시태그 추가 (문자열 리스트)
    List<String> allHashtags = hashtagRepository.findAll()
            .stream()
            .map(Hashtag::getTag)
            .distinct()
            .collect(Collectors.toList());
    response.put("hashtags", allHashtags);

    return response;
}

    // D-DAY 계산 함수
    private String calculateDDay(LocalDate createdAt, LocalDate useDate) {
        long days = ChronoUnit.DAYS.between(createdAt, useDate);
        if (days == 0) return "D-DAY";
        else if (days > 0) return "D-" + days;
        else return "D+" + Math.abs(days);
    }
}
