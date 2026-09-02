package com.payflow.payflow.transaction.dto;

import com.payflow.payflow.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(@NotNull UUID walletId, @NotNull @Positive BigDecimal amount, TransactionType type, @NotBlank String idempotencyKey) {
}
