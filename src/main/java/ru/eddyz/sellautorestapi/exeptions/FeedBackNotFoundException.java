package ru.eddyz.sellautorestapi.exeptions;

public class FeedBackNotFoundException extends RuntimeException {
    public FeedBackNotFoundException(String message) {
        super(message);
    }
}
