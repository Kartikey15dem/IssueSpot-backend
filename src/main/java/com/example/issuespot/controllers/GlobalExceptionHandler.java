package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.ErrorDto;
import com.example.issuespot.exceptions.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorDto> handleUnauthorized(UnauthorizedException e) {
    return error(HttpStatus.UNAUTHORIZED, e.getMessage(), e.getUserMessage());
  }

  @ExceptionHandler({NotFoundException.class, PostNotFoundException.class, UserNotFoundException.class})
  public ResponseEntity<ErrorDto> handleNotFound(ApiException e) {
    return error(HttpStatus.NOT_FOUND, e.getMessage(), userMessageFor(e));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDto> handleBadRequest(BadRequestException e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage(), userMessageFor(e));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).collect(Collectors.joining(", "));
    return error(HttpStatus.BAD_REQUEST, msg, "Please check the details and try again.");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorDto> handleMissingRequestParameter(MissingServletRequestParameterException e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage(), "Please check the details and try again.");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorDto> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage(), "Please check the details and try again.");
  }

  @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ErrorDto> handleMalformedRequest(Exception e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage(), "Please check the details and try again.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleGeneric(Exception e) {
    e.printStackTrace();
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage(), null);
  }

  private ResponseEntity<ErrorDto> error(HttpStatus status, String developerMessage, String userMessage) {
    return ResponseEntity.status(status).body(new ErrorDto(developerMessage, userMessage));
  }

  private String userMessageFor(ApiException e) {
    if (e.getUserMessage() != null) return e.getUserMessage();
    return switch (e.getMessage()) {
      case "Email already in use", "Email already taken" -> "This email is already in use.";
      case "Email does not exist" -> "We couldn't find an account with this email.";
      case "Invalid OTP" -> "Invalid OTP.";
      default -> null;
    };
  }
}
