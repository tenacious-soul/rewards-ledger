package com.rewards.ledger.domain;

import com.rewards.ledger.exception.InvalidPointTypeException;
import java.util.Arrays;

public enum PointType {

    PURCHASE_EARNING(1, "Purchase Earning", Direction.CREDIT),
    REFERRAL_BONUS(2, "Referral Bonus", Direction.CREDIT),
    CASHBACK(3, "Cashback", Direction.CREDIT),
    REDEMPTION(4, "Redemption", Direction.DEBIT);

    private final int id;
    private final String description;
    private final Direction direction;

    PointType(int id, String description, Direction direction) {
        this.id = id;
        this.description = description;
        this.direction = direction;
    }

    public static PointType fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() -> new InvalidPointTypeException(id));
    }

    public long toSignedPoints(long points) {
        return direction.sign * points;
    }

    public boolean isDebit() {
        return direction == Direction.DEBIT;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    private enum Direction {
        CREDIT(1),
        DEBIT(-1);

        private final int sign;

        Direction(int sign) {
            this.sign = sign;
        }
    }
}
