package com.payflow.payflow.transaction.dto;

import com.payflow.payflow.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID id, UUID walletId, BigDecimal amount , TransactionType type , Instant createdAt) {
}
