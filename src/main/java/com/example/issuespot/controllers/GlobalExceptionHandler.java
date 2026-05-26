package com.example.issuespot.controllers;
import com.example.issuespot.domain.dtos.ErrorDto;
import com.example.issuespot.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({NotFoundException.class, PostNotFoundException.class, UserNotFoundException.class})
  public ResponseEntity<ErrorDto> handleNotFound(RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getMessage())); }
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDto> handleBadRequest(BadRequestException e) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(e.getMessage())); }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(msg));
  }
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleGeneric(Exception e) { e.printStackTrace(); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto("Server error: " + e.getMessage())); }
}
