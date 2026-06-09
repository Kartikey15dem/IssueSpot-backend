package com.example.issuespot.exceptions;

public abstract class ApiException extends RuntimeException {
  private final String userMessage;

  protected ApiException(String developerMessage) {
    this(developerMessage, null);
  }

  protected ApiException(String developerMessage, String userMessage) {
    super(developerMessage);
    this.userMessage = userMessage;
  }

  public String getUserMessage() {
    return userMessage;
  }
}
