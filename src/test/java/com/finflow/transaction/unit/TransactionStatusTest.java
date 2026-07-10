package com.finflow.transaction.unit;

import com.finflow.transaction.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatusTest {

    @Test
    void happyPath() {
        assertThat(TransactionStatus.INITIATED.canTransitionTo(TransactionStatus.PENDING)).isTrue();
        assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.COMPLETED)).isTrue();
        assertThat(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.REFUNDED)).isTrue();
    }

    @Test
    void cannotSkipPending() {
        assertThat(TransactionStatus.INITIATED.canTransitionTo(TransactionStatus.COMPLETED)).isFalse();
    }

    @Test
    void cannotRefundWhatWasNeverCompleted() {
        assertThat(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.REFUNDED)).isFalse();
        assertThat(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.REFUNDED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = {"FAILED", "CANCELLED", "REFUNDED"})
    void terminalStatesHaveNoExit(TransactionStatus status) {
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.allowedTransitions()).isEmpty();
    }

    @Test
    void nullTargetIsRejected() {
        assertThat(TransactionStatus.PENDING.canTransitionTo(null)).isFalse();
    }
}
