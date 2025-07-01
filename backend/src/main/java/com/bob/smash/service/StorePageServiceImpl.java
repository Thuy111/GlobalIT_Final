package com.bob.smash.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bob.smash.dto.EstimateDTO;
import com.bob.smash.dto.StorePageDTO;
import com.bob.smash.dto.StoreUpdateRequestDTO;
import com.bob.smash.entity.Estimate;
import com.bob.smash.entity.IntroductionImage;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.repository.EstimateRepository;
import com.bob.smash.repository.IntroductionImageRepository;
import com.bob.smash.repository.PartnerInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorePageServiceImpl implements StorePageService {

    private final PartnerInfoRepository partnerRepo;
    private final IntroductionImageRepository introRepo;
    private final EstimateRepository estimateRepo;


    @Override
    public StorePageDTO getStorePage(String code, String loggedInMemberId){
        PartnerInfo partner = partnerRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 업체입니다."));

        List<String> imageURLs = introRepo.findByPartnerInfo_Bno(partner.getBno()).stream()
                .map(img -> "/uploads/" + img.getPath() + "/" + img.getSName())
                .collect(Collectors.toList());

        List<Estimate> estimates = estimateRepo.findByPartnerInfo_Bno(partner.getBno());

        List<EstimateDTO> estimateDTOs = estimates.stream()
                .map(e -> EstimateDTO.builder()
                        .idx(e.getIdx())
                        .title(e.getTitle())
                        .content(e.getContent())
                        .price(e.getPrice())
                        .isDelivery(e.getIsDelivery() != null && e.getIsDelivery() == 1)
                        .isPickup(e.getIsPickup() != null && e.getIsPickup() == 1)
                        .returnDate(e.getReturnDate())
                        .isSelected(e.getIsSelected())
                        .isReturn(e.getIsReturn() != null && e.getIsReturn() == 1)
                        .createdAt(e.getCreatedAt())
                        .modifiedAt(e.getModifiedAt())
                        // 생략된 필드들 추가 가능
                        .build())
                .collect(Collectors.toList());

        boolean isOwner = partner.getMember() != null
                && partner.getMember().getEmailId().equals(loggedInMemberId);

        return StorePageDTO.builder()
                .bno(partner.getBno())
                .code(partner.getCode())
                .name(partner.getName())
                .tel(partner.getTel())
                .region(partner.getRegion())
                .description(partner.getDescription())
                .isOwner(isOwner)
                .imageURLs(imageURLs)
                .estimates(estimateDTOs)
                .build();
    }

    @Override
    @Transactional
    public void updateStore(StoreUpdateRequestDTO dto) {
        PartnerInfo partner = partnerRepo.findById(dto.getBno())
                .orElseThrow(() -> new IllegalArgumentException("업체 정보 없음"));

        partner.changeName(dto.getName());
        partner.changeTel(dto.getTel());
        partner.changeRegion(dto.getRegion());
        partner.changeDescription(dto.getDescription());
        partnerRepo.save(partner);

       // 이미지 삭제
        if (dto.getDeleteImageIds() != null) {
            for (Integer id : dto.getDeleteImageIds()) {
                introRepo.deleteById(id); // IntroductionImage의 PK가 Integer라서 타입 일치
            }
        }

        // 이미지 업로드
        if (dto.getNewImages() != null) {
            for (MultipartFile file : dto.getNewImages()) {
                String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                String path = "uploads/intro"; // 예시 경로
                File saveFile = new File(path, storedName);
                try {
                    file.transferTo(saveFile);
                    IntroductionImage img = IntroductionImage.builder()
                            .partnerInfo(partner)
                            .sName(storedName)
                            .oName(file.getOriginalFilename())
                            .path("intro")
                            .type(file.getContentType())
                            .size(file.getSize())
                            .isMain((byte)0)
                            .orderIndex(0)
                            .build();
                    introRepo.save(img);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }
            }
        }
    }

}