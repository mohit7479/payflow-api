package com.payflow.payflow.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWalletRequest(@NotNull UUID userId, @NotBlank String currency) {
}