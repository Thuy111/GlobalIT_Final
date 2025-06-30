package com.bob.smash.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class RequestEvent extends ApplicationEvent {
  private final Integer requestIdx;
  private final Action action;
  
  public RequestEvent(Object source, Integer requestIdx, Action action) {
    super(source);
    this.requestIdx = requestIdx;
    this.action = action;
  }

  public enum Action {
    UPDATED("수정"),
    BID("낙찰"),
    GET("수령");

    private final String displayName;

    Action(String displayName) {
      this.displayName = displayName;
    }
    
    public String getDisplayName() {
      return displayName;
    }
  }
}