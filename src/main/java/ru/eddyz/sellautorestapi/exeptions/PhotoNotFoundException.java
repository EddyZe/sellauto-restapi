package ru.eddyz.sellautorestapi.exeptions;

public class PhotoNotFoundException extends ApiException {
    public PhotoNotFoundException(String message) {
        super(message, "PHOTO_NOT_FOUND");
    }
}
