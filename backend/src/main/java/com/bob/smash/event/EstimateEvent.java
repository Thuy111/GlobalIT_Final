package com.bob.smash.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class EstimateEvent extends ApplicationEvent {
  public enum Action { CREATED, UPDATED, RETURNED }
  private final Integer estimateIdx;
  private final Integer requestIdx;
  private final Action action;

  public EstimateEvent(Object source, Integer estimateIdx, Integer requestIdx, Action action) {
    super(source);
    this.estimateIdx = estimateIdx;
    this.requestIdx = requestIdx;
    this.action = action;
  }
}