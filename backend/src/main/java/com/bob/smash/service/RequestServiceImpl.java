package com.bob.smash.service;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.ImageDTO;
import com.bob.smash.dto.PaymentDTO;
import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.HashtagMapping;


import com.bob.smash.entity.Request;
import com.bob.smash.event.RequestEvent;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.HashtagMappingRepository;
import com.bob.smash.repository.HashtagRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PaymentRepository;
import com.bob.smash.repository.RequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;    
    private final EstimateRepository estimateRepository;
    private final EstimateService estimateService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final MemberRepository memberRepository;

    // hashtag
    private final HashtagRepository hashtagRepository;
    private final HashtagMappingRepository hashtagMappingRepository;

    //사진
    private final ImageService imageService;

    // 알림 생성용 이벤트 발행
    private final ApplicationEventPublisher eventPublisher;

    // 등록/////////////////////////////////////////////////////////////////////////////////
    @Override
    public Integer register(RequestDTO dto, List<MultipartFile> imageFiles) {
        Request entity = dtoToEntity(dto);
        Request saved = requestRepository.save(entity);
        // [1] 해시태그 처리
        if (dto.getHashtags() != null && !dto.getHashtags().trim().isEmpty()) {
            String[] tags = dto.getHashtags().trim().split("\\s+");
            for (String rawTag : tags) {               
                String tag = rawTag.trim().replaceFirst("^#", "");
                if (!tag.isEmpty()) {
                    // 존재하는 해시태그 있는지 확인
                    Hashtag hashtag = hashtagRepository.findByTag(tag)
                                                       .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tag(tag).build()));
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

    // 상세 페이지 보기///////////////////////////////////////////////////////////////////
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
    

    // 전체 목록 조회////////////////////////////////////(test 용, 추후 삭제 필요)
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
                    Pageable pageable = PageRequest.of(page, size, Sort.by("idx").descending());
                    Page<Request> requestPage;
                    if (search != null && !search.isBlank()) {
                        requestPage = requestRepository.findByTitleContaining(search, pageable);
                    } else {
                        requestPage = requestRepository.findAll(pageable);
                    }

                    // 요청된 의뢰서 idx 리스트 수집
                    List<Integer> requestIds = requestPage.getContent().stream()
                                                .map(Request::getIdx)
                                                .collect(Collectors.toList());

                    // 모든 해시태그 매핑 한 번에 조회
                    List<HashtagMapping> allMappings = hashtagMappingRepository.findAllByRequestIdxIn(requestIds);

                    // requestId별 해시태그 리스트로 그룹핑
                    Map<Integer, List<Hashtag>> hashtagsMap = allMappings.stream()
                        .collect(Collectors.groupingBy(
                            mapping -> mapping.getRequest().getIdx(),
                            Collectors.mapping(HashtagMapping::getHashtag, Collectors.toList())
                        ));

                    List<RequestDTO> requestDTOs = requestPage.getContent()
                        .stream()
                        .map(request -> {
                            List<Hashtag> hashtagList = hashtagsMap.getOrDefault(request.getIdx(), Collections.emptyList());
                            String hashtagsString = hashtagList.stream()
                                .map(Hashtag::getTag)
                                .collect(Collectors.joining(" "));
                            String dDay = calculateDDay(request.getCreatedAt(), request.getUseDate());
                            return RequestDTO.builder()
                                .idx(request.getIdx())
                                .title(request.getTitle())
                                .content(request.getContent())
                                .createdAt(request.getCreatedAt())
                                .useDate(request.getUseDate())
                                .isDone(request.getIsDone())
                                .hashtagList(hashtagList)
                                .hashtags(hashtagsString)
                                .dDay(dDay)
                                .build();
                        })
                        .collect(Collectors.toList());

                    Map<String, Object> response = new HashMap<>();
                    response.put("request", requestDTOs);
                    response.put("currentPage", page);
                    response.put("totalPages", requestPage.getTotalPages());
                    response.put("hasNext", requestPage.hasNext());

                    List<String> allHashtags = hashtagMappingRepository.findAll(Sort.by(Sort.Direction.DESC, "request.createdAt"))
                        .stream()
                        .map(mapping -> mapping.getHashtag().getTag())
                        .distinct()
                        .limit(12)
                        .collect(Collectors.toList());
                    response.put("hashtags", allHashtags);

                    return response;
                }


    // D-DAY 계산 함수
    private String calculateDDay(LocalDateTime createdAt, LocalDateTime useDate) {
        long seconds = ChronoUnit.SECONDS.between(createdAt, useDate);
        long days = seconds / (60 * 60 * 24); // 초 → 일 변환
        if (days == 0) return "D-DAY";
        else if (days > 0) return "D-" + days;
        else return "D+" + Math.abs(days);
    }
    

    // 임시 : 이메일에 해당하는 모든 견적 정보 삭제
    @Override
    @Transactional
    public void allDeleteByEmail(String email) {
        List<Request> requests = requestRepository.findByMember_EmailId(email); // 이메일 : 회원의 모든 의뢰서 조회
        for (Request request : requests) {
            System.out.println("request = " + request);
            hashtagMappingRepository.deleteByRequest_Idx(request.getIdx()); // 의뢰서 번호 : 관련 해시태그 매핑 삭제
            // 의뢰서 번호 : 관련 견적서 전부 삭제
            List<Estimate> estimates = estimateRepository.findByRequest_Idx(request.getIdx());
            for (Estimate estimate : estimates) {
                System.out.println("estimate = " + estimate);
                paymentRepository.deleteByEstimate_Idx(estimate.getIdx()); // 견적서 번호 : 결제 정보 전체 삭제
                // 여기에 (견적서) 이미지 관련 삭제 추가 필요
            }
            estimateRepository.deleteByRequest_Idx(request.getIdx()); // 의뢰서 번호 : 견적 정보 전체 삭제
            // 여기에 (의뢰서) 이미지 관련 삭제 추가 필요
        }
        requestRepository.deleteByMember_EmailId(email); // 이메일 : 회원의 모든 의뢰서 삭제
    }

    //의뢰서 삭제//////////////////////////////////////////////////////////////////////////////
    @Override
    @Transactional
    public void delete(Integer idx) {
        Request request = requestRepository.findById(idx)
                                           .orElseThrow(() -> new IllegalArgumentException(idx + "번 의뢰서를 찾을 수 없습니다."));
        // [1] 첨부 이미지 삭제
        imageService.deleteImagesByTarget("request", idx);
        // [2] 해시태그 매핑 삭제
        hashtagMappingRepository.deleteByRequest_Idx(idx);
        // [3] 해당 견적서들 삭제
        List<Estimate> estimateList = estimateRepository.findByRequest_Idx(idx);
        for (Estimate estimate : estimateList) {
            estimateService.deleteWithImage(estimate.getIdx());
        }
        // [4] 의뢰서 삭제
        requestRepository.delete(request); 
    }

    //수정/////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    @Transactional
    public void modify(RequestDTO dto,List<MultipartFile> newImages,List<Integer> deleteImageIds){
        // [1] 기존 의뢰서 가져오기
        Request request = requestRepository.findById(dto.getIdx())
                                           .orElseThrow(() -> new IllegalArgumentException("의뢰서를 찾을 수 없습니다: " + dto.getIdx()));
        // [2] 값 변경
        request.changeTitle(dto.getTitle());
        request.changeContent(dto.getContent());
        request.changeUseDate(dto.getUseDate());
        request.changeUseRegion(dto.getUseRegion());
        request.changeIsModify((byte) 1);
        // [3] 기존 해시태그 매핑 삭제
        hashtagMappingRepository.deleteByRequest_Idx(request.getIdx());
        // [4] 새로운 해시태그 추가
        if (dto.getHashtags() != null && !dto.getHashtags().trim().isEmpty()) {
            String[] tags = dto.getHashtags().trim().split("\\s+");
            for (String rawTag : tags) {
                String tag = rawTag.trim();
                if (!tag.isEmpty()) {
                    Hashtag hashtag = hashtagRepository.findByTag(tag)
                                                       .orElseGet(() -> hashtagRepository.save(Hashtag.builder().tag(tag).build()));
                    HashtagMapping mapping = HashtagMapping.builder()
                                                           .hashtag(hashtag)
                                                           .request(request)
                                                           .build();
                    hashtagMappingRepository.save(mapping);
                }
            }
        }
        // [5] 기존 이미지 삭제  (사용자 지정하는 이미지만 삭제)      
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Integer imageId : deleteImageIds) {
                imageService.deleteImageFromTarget("request", dto.getIdx(), imageId);
            }
        }
        // [6] 새로운 이미지 업로드
        if (newImages != null && !newImages.isEmpty()) {
            newImages.stream()
                     .filter(file -> !file.isEmpty())
                     .forEach(file -> {
                        try {
                            imageService.uploadAndMapImage("request", dto.getIdx(), file);
                        } catch (Exception e) {
                            log.error("이미지 업로드 실패: {}", e.getMessage());
                        }
            });
        }
        // 의뢰서 수정 이벤트 발행(알림 생성용)
        eventPublisher.publishEvent(new RequestEvent(this, dto.getIdx(), RequestEvent.Action.UPDATED));
    }

    // 낙찰현황(isDone) 변경
    @Override
    @Transactional
    public Integer changeIsDone(Integer requestidx,
                                Integer estimateIdx, 
                                String memberEmail,
                                String partnerBno,
                                Integer price) {
        Request request = requestRepository.findById(requestidx)
                                           .orElseThrow(() -> new IllegalArgumentException("의뢰서를 찾을 수 없습니다: " + requestidx));
        PaymentDTO savedPayment = paymentService.savePayment(memberEmail, partnerBno, estimateIdx, price);
        if(savedPayment == null){
            throw new IllegalArgumentException("결제 저장에 실패했습니다.");
        }
        // 낙찰된 의뢰서 상태 1로 변경
        request.changeIsDone((byte) 1);
        requestRepository.save(request);
        // 해당 의뢰서에 속한 견적서들 상태 변경
        EstimateDTO estimateDTO = estimateService.get(estimateIdx);
        estimateService.changeSelectStatus(estimateDTO);
        // 의뢰서 낙찰 이벤트 발행(알림 생성용)
        eventPublisher.publishEvent(new RequestEvent(this, requestidx, RequestEvent.Action.BID));
        return savedPayment.getIdx();
    }
    // 사용 일시가 지난 견적서 자동 미낙찰
    @Override
    @Transactional
    @Scheduled(cron = "0 0/10 * * * ?") // 10분마다, 필요시 조정
    public void autoBid() {
        List<Request> expiredRequests = requestRepository.findByUseDateBeforeAndIsDone(LocalDateTime.now(), (byte)0);
        for (Request request : expiredRequests) {
            // 견적서 서비스의 자동 낙찰 처리 호출
            estimateService.autoSelect(request.getIdx());
        }
    }

    // 물품 수령 확인(isGet) 변경
    @Override
    public Integer changeIsGet(Integer idx) {
        Request request = requestRepository.findById(idx)
                                           .orElseThrow(() -> new IllegalArgumentException("의뢰서를 찾을 수 없습니다: " + idx));
        request.changeIsGet((byte) 1); // isGet을 1로 변경
        requestRepository.save(request);
        // 의뢰서 수령 이벤트 발행(알림 생성용)
        eventPublisher.publishEvent(new RequestEvent(this, idx, RequestEvent.Action.GET));
        return idx;
    }

    // DTO → Entity 변환
    Request dtoToEntity(RequestDTO dto) {
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
                    .member(memberRepository.findByEmailId(dto.getWriterEmail())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지않는 회원입니다.")))
                    .build();
    }

    // Entity → DTO 변환
    RequestDTO entityToDto(Request request,List<Hashtag> hashtags) {  //hashtag
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
                        .isGet(request.getIsGet())
                        .writerEmail(request.getMember().getEmailId())      
                        .build();
    }
}