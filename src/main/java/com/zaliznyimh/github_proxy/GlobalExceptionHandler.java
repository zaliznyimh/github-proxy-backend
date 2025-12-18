package com.zaliznyimh.github_proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ApplicationErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApplicationErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    ResponseEntity<ApplicationErrorResponse> handleGithubForbidden(HttpClientErrorException.Forbidden ex) {
        log.warn("GitHub API Forbidden: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApplicationErrorResponse(403, "GitHub API rate limit exceeded. Please try again later"));
    }
}
