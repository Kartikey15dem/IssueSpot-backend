package com.example.issuespot.exceptions;
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) { super(message); }
}
