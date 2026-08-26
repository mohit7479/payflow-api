package com.payflow.payflow.transaction;

import com.payflow.payflow.common.exception.ResourceNotFoundException;
import com.payflow.payflow.transaction.dto.CreateTransactionRequest;
import com.payflow.payflow.transaction.dto.TransactionResponse;
import com.payflow.payflow.wallet.Wallet;
import com.payflow.payflow.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        BigDecimal newBalance;
        Wallet wallet = walletRepository.findById(request.walletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        if (request.type() == TransactionType.CREDIT) {
            newBalance = wallet.getBalance().add(request.amount());
        } else {
            newBalance = wallet.getBalance().subtract(request.amount());
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        Transaction transaction = new Transaction(wallet, request.amount(), request.type());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(savedTransaction.getId(), savedTransaction.getWallet().getId(), savedTransaction.getAmount(), savedTransaction.getType(), savedTransaction.getCreatedAt());
    }


}

