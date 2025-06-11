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

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 1000, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "use_date", nullable = false)
    private LocalDateTime useDate;

    @Column(name = "use_region", length = 50, nullable = false)
    private String useRegion;

    @Column(name = "is_done", nullable = false)
    private Byte isDone;

    @Column(name = "is_get", nullable = false)
    private Byte isGet;

    public void changeTitle(String title) {this.title = title;}
    public void changeContent(String content) {this.content = content;}
    public void changeUseDate(LocalDateTime useDate) {this.useDate = useDate;}
    public void changeUseRegion(String useRegion) {this.useRegion = useRegion;}
    public void changeIsDone(Byte isDone) {this.isDone = isDone;}
    public void changeIsGet(Byte isGet) {this.isGet = isGet;}
}