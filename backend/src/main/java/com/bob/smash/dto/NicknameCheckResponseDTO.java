package com.bob.smash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponseDTO {
    private boolean duplicated;
}