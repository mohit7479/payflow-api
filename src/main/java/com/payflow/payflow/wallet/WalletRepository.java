package com.payflow.payflow.wallet;

import com.payflow.payflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet,UUID> {

    List<Wallet> findByUser(User user);
}
