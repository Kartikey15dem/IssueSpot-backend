package com.example.issuespot.exceptions;
public class BadRequestException extends ApiException {
  public BadRequestException(String message) { super(message); }
  public BadRequestException(String developerMessage, String userMessage) { super(developerMessage, userMessage); }
}
