package com.example.issuespot.exceptions;
public class NotFoundException extends ApiException {
  public NotFoundException(String message) { super(message); }
  public NotFoundException(String developerMessage, String userMessage) { super(developerMessage, userMessage); }
}
