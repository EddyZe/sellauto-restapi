package ru.eddyz.sellautorestapi.exeptions;




public class AccountException extends ApiException{

    public AccountException(String msg, String code) {
        super(msg, code);
    }
}
