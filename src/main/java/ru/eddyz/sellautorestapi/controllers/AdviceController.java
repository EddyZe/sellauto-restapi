package ru.eddyz.sellautorestapi.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.eddyz.sellautorestapi.exeptions.AccountException;
import ru.eddyz.sellautorestapi.exeptions.AccountNotFoundException;
import ru.eddyz.sellautorestapi.exeptions.AuthException;

@RestControllerAdvice
public class AdviceController {
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<?> handleAccountException(AccountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFoundException(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> authExceptionHandler(AuthException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail
                        .forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail
                        .forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }
}
