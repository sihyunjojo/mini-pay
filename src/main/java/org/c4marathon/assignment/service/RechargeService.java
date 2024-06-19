package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.c4marathon.assignment.util.AccountType.RECHARGE_INCREMENT;

@Service
@RequiredArgsConstructor
public class RechargeService {
    private AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void rechargeIncrements(BigDecimal requiredAmount, Account account) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal amountToRecharge = requiredAmount.subtract(currentBalance);

        while (amountToRecharge.compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(account.getBalance().add(RECHARGE_INCREMENT));
            accountRepository.save(account);
            amountToRecharge = amountToRecharge.subtract(RECHARGE_INCREMENT);
        }
    }
}
