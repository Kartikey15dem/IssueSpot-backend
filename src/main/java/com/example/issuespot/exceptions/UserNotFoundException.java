package com.example.issuespot.exceptions;
public class UserNotFoundException extends ApiException {
    public UserNotFoundException(String message) { super(message); }
    public UserNotFoundException(String developerMessage, String userMessage) { super(developerMessage, userMessage); }
}
