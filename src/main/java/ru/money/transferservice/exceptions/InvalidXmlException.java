package ru.money.transferservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Invalid XML.")  // 400
public class InvalidXmlException extends RuntimeException {

    public InvalidXmlException(String message) {
        super(message);
    }
}
