package com.bob.smash.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class EstimateEvent extends ApplicationEvent {
  private final Integer estimateIdx;
  private final Integer requestIdx;
  private final Action action;
  
  public EstimateEvent(Object source, Integer estimateIdx, Integer requestIdx, Action action) {
    super(source);
    this.estimateIdx = estimateIdx;
    this.requestIdx = requestIdx;
    this.action = action;
  }

  public enum Action {
    CREATED("생성"), 
    UPDATED("수정"),
    RETURNED ("반납");

    private final String displayName;

    Action(String displayName) {
      this.displayName = displayName;
    }
    
    public String getDisplayName() {
      return displayName;
    }
  }
}