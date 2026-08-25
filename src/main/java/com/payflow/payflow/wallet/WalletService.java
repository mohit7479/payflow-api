package com.payflow.payflow.wallet;

import com.payflow.payflow.user.User;
import com.payflow.payflow.user.UserRepository;
import com.payflow.payflow.wallet.dto.CreateWalletRequest;
import com.payflow.payflow.wallet.dto.WalletResponse;
import org.springframework.stereotype.Service;


@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }


    public WalletResponse createWallet(CreateWalletRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = new Wallet(user, request.currency());
        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(savedWallet.getId(), savedWallet.getUser().getId(), savedWallet.getCurrency(), savedWallet.getBalance(), savedWallet.getCreatedAt());
    }
}
