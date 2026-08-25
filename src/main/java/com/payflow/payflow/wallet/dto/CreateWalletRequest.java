package com.payflow.payflow.wallet.dto;

import java.util.UUID;

public record CreateWalletRequest(UUID userId, String currency) {
}