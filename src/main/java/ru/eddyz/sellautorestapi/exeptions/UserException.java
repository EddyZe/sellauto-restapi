package ru.eddyz.sellautorestapi.exeptions;




public class UserException extends ApiException{

    public UserException(String msg) {
        super(msg, "USER_NOT_FOUND");
    }
}
