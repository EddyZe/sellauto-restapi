package ru.eddyz.sellautorestapi.util;

import org.springframework.validation.BindingResult;

public class BindingResultHelper {

    public static String buildFieldErrorMessage(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();

        bindingResult.getFieldErrors()
                .forEach(fieldError -> {
                    var msg = "%s - %s; ".formatted(fieldError.getField(), fieldError.getDefaultMessage());
                    sb.append(msg);
                });

        return sb.toString();
    }
}
