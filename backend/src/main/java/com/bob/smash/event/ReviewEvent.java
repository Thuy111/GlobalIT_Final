package com.bob.smash.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class ReviewEvent extends ApplicationEvent {
  private final Integer reviewIdx;
  private final Integer estimateIdx;
  private final Action action;

  public ReviewEvent(Object source, Integer reviewIdx, Integer estimateIdx, Action action) {
    super(source);
    this.reviewIdx = reviewIdx;
    this.estimateIdx = estimateIdx;
    this.action = action;
  }
  
  public enum Action {
    CREATED("작성"), 
    UPDATED("수정");

    private final String displayName;

    Action(String displayName) {
      this.displayName = displayName;
    }
    
    public String getDisplayName() {
      return displayName;
    }
  }
}