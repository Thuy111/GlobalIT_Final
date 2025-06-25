package com.bob.smash.event;

import java.time.Clock;

import org.springframework.context.ApplicationEvent;

public class EstimateCreatedEvent extends ApplicationEvent {
  private final Integer estimateIdx;
  private final String partnerBno;
  private final String partnerName;
  private final Integer requestIdx;
  private final String requsterId;
  private final Integer price;

  public EstimateCreatedEvent(Object source, Integer estimateIdx, Integer requestIdx, String requsterId, String partnerBno, String partnerName, Integer price) {
    super(source);
    this.estimateIdx = estimateIdx;
    this.partnerBno = partnerBno;
    this.partnerName = partnerName;
    this.requestIdx = requestIdx;
    this.requsterId = requsterId;
    this.price = price;
  }
}