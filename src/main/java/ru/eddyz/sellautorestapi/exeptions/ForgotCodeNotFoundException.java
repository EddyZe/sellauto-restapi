package ru.eddyz.sellautorestapi.exeptions;

public class ForgotCodeNotFoundException extends RuntimeException {
    public ForgotCodeNotFoundException(String message) {
        super(message);
    }
}
