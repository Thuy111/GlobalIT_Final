package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartnerInfo {
    @Id
    @Column(length = 25)
    private String bno;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 15, nullable = false)
    private String tel;

    @Column(length = 50, nullable = false)
    private String region;

    @Column(length = 3000, nullable = false)
    private String description;
    
    @Column(length = 64, nullable = false, unique = true)
    private String code;
    
    @Column(name = "visit_cnt")
    private Integer visitCnt;

    public void changeName(String name) {this.name = name;}
    public void changeTel(String tel) {this.tel = tel;}
    public void changeRegion(String region) {this.region = region;}
    public void changeDescription(String description) {this.description = description;}
    public void changeVisitCount(Integer visitCnt) {this.visitCnt = visitCnt;}
}