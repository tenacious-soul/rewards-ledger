package com.rewards.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rewards.ledger.exception.InvalidPointTypeException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PointTypeTest {

    @ParameterizedTest
    @CsvSource({
            "1, PURCHASE_EARNING, 100",
            "2, REFERRAL_BONUS, 100",
            "3, CASHBACK, 100",
            "4, REDEMPTION, -100"
    })
    void resolvesTypeAndAppliesSign(int id, PointType expectedType, long expectedSignedPoints) {
        PointType type = PointType.fromId(id);

        assertThat(type).isEqualTo(expectedType);
        assertThat(type.toSignedPoints(100)).isEqualTo(expectedSignedPoints);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5, -1})
    void rejectsUnknownId(int id) {
        assertThatThrownBy(() -> PointType.fromId(id)).isInstanceOf(InvalidPointTypeException.class);
    }

    @ParameterizedTest
    @CsvSource({"PURCHASE_EARNING, false", "REFERRAL_BONUS, false", "CASHBACK, false", "REDEMPTION, true"})
    void onlyRedemptionIsDebit(PointType type, boolean expectedDebit) {
        assertThat(type.isDebit()).isEqualTo(expectedDebit);
    }
}
