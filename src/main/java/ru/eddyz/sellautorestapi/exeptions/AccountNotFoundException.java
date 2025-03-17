package ru.eddyz.sellautorestapi.exeptions;


public class AccountNotFoundException extends ApiException{

    public AccountNotFoundException(String msg) {
        super(msg, "ACCOUNT_NOT_FOUND");
    }
}
