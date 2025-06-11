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
public class Estimate {
		@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @ManyToOne
    @JoinColumn(name = "request_idx", nullable = false)
    private Request request;

    @ManyToOne
    @JoinColumn(name = "partner_bno", nullable = false)
    private PartnerInfo partnerInfo;

    @Column(length = 1000, nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer price;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "is_delivery", nullable = false)
    private Byte isDelivery;

    @Column(name = "is_pickup", nullable = false)
    private Byte isPickup;

    @Column(name = "is_selected", nullable = false)
    private Byte isSelected;

    @Column(name = "is_return", nullable = false)
    private Byte isReturn;

    public void changeTitle(String title) {this.title = title;}
    public void changeContent(String content) {this.content = content;}
    public void changePrice(Integer price) {this.price = price;}
    public void changeReturnDate(LocalDateTime returnDate) {this.returnDate = returnDate;}
    public void changeIsDelivery(Byte isDelivery) {this.isDelivery = isDelivery;}
    public void changeIsPickup(Byte isPickup) {this.isPickup = isPickup;}
    public void changeIsSelected(Byte isSelected) {this.isSelected = isSelected;}
    public void changeIsReturn(Byte isReturn) {this.isReturn = isReturn;}
}