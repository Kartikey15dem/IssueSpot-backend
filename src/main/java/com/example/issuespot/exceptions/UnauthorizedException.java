package com.example.issuespot.exceptions;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(String message) {
    super(message, "Please sign in to continue.");
  }
}
