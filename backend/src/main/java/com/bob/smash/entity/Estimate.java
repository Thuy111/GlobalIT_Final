package com.bob.smash.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Estimate {
		@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_idx", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_bno", nullable = false)
    private PartnerInfo partnerInfo;

    @Column(length = 1000, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Integer price;
    
    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;
    
    @Column(name = "is_delivery", nullable = false)
    private Byte isDelivery; // 0: 배달 불가, 1: 배달 가능
    
    @Column(name = "is_pickup", nullable = false)
    private Byte isPickup; // 0: 픽업 불가, 1: 픽업 가능
    
    @Column(name = "is_selected", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isSelected = 0; // 0: 낙찰 대기, 1: 미 낙찰, 2: 낙찰
    
    @Column(name = "is_return", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte isReturn = 0; // 0: 반납 대기, 1: 반납 완료
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "modified_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modifiedAt;
    
    public void changeTitle(String title) {this.title = title;}
    public void changeContent(String content) {this.content = content;}
    public void changePrice(Integer price) {this.price = price;}
    public void changeReturnDate(LocalDateTime returnDate) {this.returnDate = returnDate;}
    public void changeIsDelivery(Byte isDelivery) {this.isDelivery = isDelivery;}
    public void changeIsPickup(Byte isPickup) {this.isPickup = isPickup;}
    public void changeIsSelected(Byte isSelected) {this.isSelected = isSelected;}
    public void changeIsReturn(Byte isReturn) {this.isReturn = isReturn;}
    public void changeModifiedAt(LocalDateTime modifiedAt) {this.modifiedAt = modifiedAt;}
}