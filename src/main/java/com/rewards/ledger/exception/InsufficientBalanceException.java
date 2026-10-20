package com.rewards.ledger.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends LedgerException {

    public InsufficientBalanceException(long availableBalance, long requestedPoints) {
        super(HttpStatus.UNPROCESSABLE_ENTITY,
                "Insufficient balance: requested %d points but only %d available"
                        .formatted(requestedPoints, availableBalance));
    }
}
