package ru.eddyz.sellautorestapi.exeptions;





public class CarNotFoundException extends ApiException{
    public CarNotFoundException(String message) {
        super(message, "CAR_NOT_FOUND");
    }
}
