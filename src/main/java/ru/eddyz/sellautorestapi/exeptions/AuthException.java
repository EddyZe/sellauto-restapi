package ru.eddyz.sellautorestapi.exeptions;

public class AuthException extends ApiException{
    public AuthException(String message, String errorCode){
        super(message, errorCode);
    }
}
