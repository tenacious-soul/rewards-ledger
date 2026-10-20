package com.rewards.ledger.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends LedgerException {

    public DuplicateEmailException(String email) {
        super(HttpStatus.CONFLICT, "A member with email '%s' already exists".formatted(email));
    }
}
