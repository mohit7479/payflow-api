package com.payflow.payflow.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(UUID id, UUID userId, String currency, BigDecimal balance, Instant createdAt) {
}
