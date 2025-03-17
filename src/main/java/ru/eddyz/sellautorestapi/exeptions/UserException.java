package ru.eddyz.sellautorestapi.exeptions;




public class UserException extends RuntimeException{

    public UserException(String msg) {
        super(msg);
    }
}
