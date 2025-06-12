package com.bob.smash.service;

import com.bob.smash.dto.RequestDTO;
import com.bob.smash.entity.Hashtag;
import com.bob.smash.entity.HashtagMapping;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.Request;
import com.bob.smash.repository.HashtagMappingRepository;
import com.bob.smash.repository.HashtagRepository;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final MemberRepository memberRepository;

    private final HashtagRepository hashtagRepository;
    private final HashtagMappingRepository hashtagMappingRepository;

    // 등록///////////////////////////////////////////////////
    @Override
    public Integer register(RequestDTO dto, Member member) {
    Request entity = dtoToEntity(dto, member);
    Request saved = requestRepository.save(entity);

    // 2. 해시태그 처리
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
        return entityToDto(request, hashtags);
        }    
        return null;
    }
    

    // 전체 목록 조회////////////////////////////////////
    @Override
    public List<RequestDTO> getList() {   
        List<Request> list = requestRepository.findAll();
        // return list.stream().map(this::entityToDto).collect(Collectors.toList());
        return list.stream().map(request -> {
        List<Hashtag> hashtags = hashtagMappingRepository.findHashtagsByRequestIdx(request.getIdx());
                return entityToDto(request, hashtags);
        }).collect(Collectors.toList());
    }

}
