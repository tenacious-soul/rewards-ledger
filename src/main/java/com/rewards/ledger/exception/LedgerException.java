package com.rewards.ledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public abstract class LedgerException extends ErrorResponseException {

    protected LedgerException(HttpStatus status, String detail) {
        super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
    }

    @Override
    public String getMessage() {
        return getBody().getDetail();
    }
}
