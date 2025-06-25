package com.bob.smash.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartnerInfoDTO {
  private String bno;
  private String memberId; // Member 객체 대신 ID만 노출
  private String name;
  private String tel;
  private String region;
  private String description;
  private Integer visitCnt;
}
