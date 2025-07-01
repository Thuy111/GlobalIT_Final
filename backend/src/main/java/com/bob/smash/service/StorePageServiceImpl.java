package com.bob.smash.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Paths;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Value("${com.bob.upload.path}")
    private String uploadPath;

    @Override
    public StorePageDTO getStorePage(String code, String loggedInMemberId) {
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

        // 업체 기본 정보 업데이트
        partner.changeName(dto.getName());
        partner.changeTel(dto.getTel());
        partner.changeRegion(dto.getRegion());
        partner.changeDescription(dto.getDescription());
        partnerRepo.save(partner);

        // 이미지 삭제 처리
        if (dto.getDeleteImageIds() != null) {
            for (Integer id : dto.getDeleteImageIds()) {
                introRepo.findById(id).ifPresent(img -> {
                    // 실제 파일 삭제
                    String fullPath = Paths.get(uploadPath, img.getPath(), img.getSName()).toAbsolutePath().toString();
                    File file = new File(fullPath);
                    if (file.exists()) {
                        file.delete();
                    }

                    // DB에서 삭제
                    introRepo.deleteById(id);
                });
            }
        }

        // 이미지 순서 및 대표 이미지 설정
        if (dto.getImageOrders() != null) {
            for (StoreUpdateRequestDTO.ImageOrder imageOrder : dto.getImageOrders()) {
                introRepo.findById(imageOrder.getImageId()).ifPresent(img -> {
                    img.changeOrderIndex(imageOrder.getOrderIndex());
                    img.changeIsMain((byte) (imageOrder.isMain() ? 1 : 0));
                    introRepo.save(img);
                });
            }
        }

        // 새 이미지 업로드
        if (dto.getNewImages() != null) {
            for (MultipartFile file : dto.getNewImages()) {
                saveIntroductionImage(partner, file);
            }
        }
    }

    private void saveIntroductionImage(PartnerInfo partner, MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String saveName = uuid + ext;

        // 절대 경로 확보 및 폴더 생성
        String absUploadPath = Paths.get(uploadPath, "intro").toAbsolutePath().toString();
        File dir = new File(absUploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File savePath = new File(dir, saveName);

        // 로그 추가
        System.out.println("파일 경로: " + savePath.getAbsolutePath());
        try {
            file.transferTo(savePath);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + savePath.getAbsolutePath(), e);
        }

        // DB에 저장
        IntroductionImage img = IntroductionImage.builder()
                .partnerInfo(partner)
                .sName(saveName)
                .oName(originalName)
                .path("intro")
                .type(file.getContentType())
                .size(file.getSize())
                .isMain((byte) 0)
                .orderIndex(0)
                .build();
        introRepo.save(img);
    }
}