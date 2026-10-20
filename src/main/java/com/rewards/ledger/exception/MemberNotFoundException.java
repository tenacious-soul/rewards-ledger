package com.rewards.ledger.exception;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends LedgerException {

    public MemberNotFoundException(Long memberId) {
        super(HttpStatus.NOT_FOUND, "Member %d not found".formatted(memberId));
    }
}
