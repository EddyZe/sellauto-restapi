package ru.eddyz.sellautorestapi.exeptions;

public class ChatNotFoundException extends RuntimeException{
    public ChatNotFoundException(String message) {
        super(message);
    }
}
