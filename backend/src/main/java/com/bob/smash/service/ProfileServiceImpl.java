package com.bob.smash.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.entity.Member;
import com.bob.smash.entity.PartnerInfo;
import com.bob.smash.entity.ProfileImage;
import com.bob.smash.repository.MemberRepository;
import com.bob.smash.repository.PartnerInfoRepository;
import com.bob.smash.repository.ProfileImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{
    
    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final PartnerInfoRepository partnerInfoRepository;

    @Override
    public ProfileDTO getProfileByEmail (String emailId){
        // 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmailId(emailId)
                        .orElseThrow(() -> {
                            log.warn("회원 정보 조회 실패 - 존재하지 않는 emailId: {}", emailId);
                            return new IllegalArgumentException("회원이 존재하지 않습니다.");
    });
        

        // 프로필 이미지 조회
        Optional<ProfileImage> profileOpt = profileImageRepository.findById(member.getEmailId());
        String profileImageUrl = profileOpt
                .map(img -> img.getPath() + "/" + img.getSName())
                .orElse(null); 


        // 파트너 정보 여부 확인
        Optional<PartnerInfo> partnerOpt = partnerInfoRepository.findByMember_EmailId(emailId);
        log.info("findByMember_EmailId({}) 결과: isPresent={}", emailId, partnerOpt.isPresent());

        boolean isPartner = partnerInfoRepository.findByMember_EmailId(emailId).isPresent();
  
        ProfileDTO.ProfileDTOBuilder builder = ProfileDTO.builder()
                .email(member.getEmailId())
                .nickname(member.getNickname())
                .loginType(member.getLoginType())
                .isPartner(isPartner)
                .profileImageUrl(profileImageUrl);
                

        if (isPartner) {
            PartnerInfo p = partnerOpt.get();
            builder.bno(p.getBno());
            builder.partnerName(p.getName());

        }

        ProfileDTO profileDTO = builder.build();

        log.info("ProfileDTO 반환값: {}", profileDTO);
        return profileDTO;
    }
}
