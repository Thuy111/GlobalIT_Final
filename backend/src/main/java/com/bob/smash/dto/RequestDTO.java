// RequestDTO.java
package com.bob.smash.dto;

import lombok.*;
import java.time.LocalDateTime;
// import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    private Integer idx;
    private String title;
    private String content;
    private LocalDateTime useDate;
    private String useRegion;
    private Byte isDelivery;
    // private MultipartFile imageFile; // 첨부 사진
    private String emailId; //entity member와 매핑
}
