package ru.eddyz.sellautorestapi.exeptions;

public class ColorNotFoundException extends RuntimeException {
    public ColorNotFoundException(String message) {
        super(message);
    }
}
