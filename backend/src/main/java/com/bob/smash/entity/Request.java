package com.bob.smash.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 1000, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;
    
    @Column(name = "use_date", nullable = false)
    private LocalDateTime useDate;
    
    @Column(name = "use_region", length = 50, nullable = false)
    private String useRegion;
    
    @Column(name = "is_done", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isDone = 0; // 0: 대기, 1: 완료
    
    @Column(name = "is_get", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isGet = 0; // 0: 미 수령, 1: 수령
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "is_modify", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isModify=0; // 0: 미 수정, 1: 수정됨

    public void changeTitle(String title) {this.title = title;}
    public void changeContent(String content) {this.content = content;}
    public void changeUseDate(LocalDateTime useDate) {this.useDate = useDate;}
    public void changeUseRegion(String useRegion) {this.useRegion = useRegion;}
    public void changeIsDone(Byte isDone) {this.isDone = isDone;}
    public void changeIsGet(Byte isGet) {this.isGet = isGet;}
    public void changeIsModify(Byte isModify) {this.isModify = isModify;}
}