package com.bob.smash.service;

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.entity.*;
import com.bob.smash.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
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
        Member member = memberRepository.findById(emailId)
                        .orElseThrow(() -> {
                            log.warn("회원 정보 조회 실패 - 존재하지 않는 emailId: {}", emailId);
                            return new IllegalArgumentException("회원이 존재하지 않습니다.");
    });
        
        boolean isPartner = partnerInfoRepository.findByMember_EmailId(emailId).isPresent();

        Optional<ProfileImage> profileOpt = profileImageRepository.findById(member.getEmailId());

        String profileImageUrl = profileOpt
                .map(img -> img.getPath() + "/" + img.getSName())
                .orElse(null);


        return  ProfileDTO.builder()
                    .email(member.getEmailId())
                    .nickname(member.getNickname())
                    .loginType(member.getLoginType())
                    .isPartner(isPartner)
                    .profileImageUrl(profileImageUrl)
                    .build();
    }
}
