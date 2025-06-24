package com.bob.smash.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UpdateRequestDTO {
    private String nickname;
    private String tel;
    private String region;

    private String partnerName;
    private String partnerTel;
    private String partnerRegion;
    private String description;
}
