package com.bob.smash.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class RequestEvent extends ApplicationEvent {
  public enum Action { UPDATED, BID, GET }
  private final Integer requestIdx;
  private final Action action;

  public RequestEvent(Object source, Integer requestIdx, Action action) {
    super(source);
    this.requestIdx = requestIdx;
    this.action = action;
  }
}