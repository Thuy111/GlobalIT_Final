package com.bob.smash.service;

import com.bob.smash.dto.ProfileDTO;
import com.bob.smash.entity.*;
import com.bob.smash.repository.*;
import com.bob.smash.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{
    
    private final MemberRepository memberRepo;
    private final ProfileImageRepository profileRepo;
    private final PartnerInfoRepository partnerRepo;

    @Override
    public ProfileDTO getProfileByEmail (String emailId){
        Member member = memberRepo.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        ProfileImage profileImage = profileRepo.findByMember(member).orElse(null);
         boolean isPartner = partnerRepo.findByMember(member).isPresent();

        String imagePath = profileImage != null
            ? profileImage.getPath() + "/" + profileImage.getSName()
            : null;

        return new ProfileDTO(
            member.getEmailId(),
            member.getNickname(),
            member.getLoginType().name(),
            imagePath,
            isPartner ? "PARTNER" : "USER"
        );
    }
}
