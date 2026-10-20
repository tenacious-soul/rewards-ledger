package com.rewards.ledger.exception;

import org.springframework.http.HttpStatus;

public class InvalidPointTypeException extends LedgerException {

    public InvalidPointTypeException(int pointTypeId) {
        super(HttpStatus.BAD_REQUEST, "Invalid point_type_id %d".formatted(pointTypeId));
    }
}
