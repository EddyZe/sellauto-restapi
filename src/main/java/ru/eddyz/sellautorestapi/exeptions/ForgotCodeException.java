package ru.eddyz.sellautorestapi.exeptions;

public class ForgotCodeException extends RuntimeException{
    public ForgotCodeException(String message){
        super(message);
    }
}
