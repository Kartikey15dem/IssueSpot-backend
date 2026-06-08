package com.example.issuespot.exceptions;
public class PostNotFoundException extends ApiException {
    public PostNotFoundException(String message) { super(message); }
    public PostNotFoundException(String developerMessage, String userMessage) { super(developerMessage, userMessage); }
}
