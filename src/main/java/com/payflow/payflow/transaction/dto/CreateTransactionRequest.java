package com.payflow.payflow.transaction.dto;

import com.payflow.payflow.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(UUID walletId, BigDecimal amount, TransactionType type) {
}
