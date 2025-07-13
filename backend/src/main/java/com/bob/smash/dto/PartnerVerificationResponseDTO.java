package com.bob.smash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerVerificationResponseDTO {
    
    private boolean valid;
    private String message;
}
