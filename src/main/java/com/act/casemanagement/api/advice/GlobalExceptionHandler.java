package com.act.casemanagement.api.advice;

import com.act.casemanagement.domain.exception.DomainException;
import com.act.casemanagement.domain.exception.EngineAdapterException;
import com.act.casemanagement.domain.exception.ForbiddenOperationException;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * RFC 7807 ProblemDetail error responses — mirrors bs-filing-core-server GlobalExceptionHandler.
 *
 * Mapping:
 *   DomainException               → 422 Unprocessable Entity
 *   ForbiddenOperationException   → 403 Forbidden
 *   ResourceNotFoundException     → 404 Not Found
 *   MethodArgumentNotValidException → 400 Bad Request
 *   EngineAdapterException        → 503 Service Unavailable
 *   Throwable (fallback)          → 500 Internal Server Error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenOperationException.class)
    public ProblemDetail handleForbidden(ForbiddenOperationException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setType(URI.create("urn:problem:forbidden-operation"));
        pd.setTitle("Forbidden Operation");
        pd.setProperty("operation", ex.getOperation());
        pd.setProperty("resourceId", ex.getResourceId());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("urn:problem:resource-not-found"));
        pd.setTitle("Resource Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        log.warn("Domain rule violation: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("urn:problem:domain-rule-violation"));
        pd.setTitle("Domain Rule Violation");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Validation failed");
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setType(URI.create("urn:problem:validation-error"));
        pd.setTitle("Validation Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(EngineAdapterException.class)
    public ProblemDetail handleEngineAdapter(EngineAdapterException ex) {
        log.error("Engine adapter failure: {}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "A downstream service is temporarily unavailable");
        pd.setType(URI.create("urn:problem:engine-adapter-failure"));
        pd.setTitle("Engine Adapter Failure");
        pd.setProperty("adapter", ex.getAdapterName());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleAll(Throwable ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setType(URI.create("urn:problem:internal-error"));
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
