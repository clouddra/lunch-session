package com.govtech.lunchsession.controllers;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(value = {DataIntegrityViolationException.class})
  public ResponseEntity<Void> UnexpectedRollbackException(DataIntegrityViolationException ex) {
    if (ex.getCause() instanceof ConstraintViolationException) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    throw ex;
  }
}
