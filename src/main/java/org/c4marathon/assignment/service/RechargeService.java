package org.c4marathon.assignment.service;

import org.c4marathon.assignment.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.c4marathon.assignment.util.AccountType.RECHARGE_INCREMENT;

@Service
public class RechargeService {

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void rechargeIncrements(BigDecimal requiredAmount, Account account) {
        BigDecimal currentBalance = account.getBalance();
        BigDecimal amountToRecharge = requiredAmount.subtract(currentBalance);

        BigDecimal rechargeBalance = BigDecimal.ZERO;
        while (amountToRecharge.compareTo(BigDecimal.ZERO) > 0) {
            // 추후 연결계좌가 주어진다면 연결계좌에 돈이 없을시 에러 발생시키고 기존에 가져간 돈 다시 돌려줘야함.
            rechargeBalance = rechargeBalance.add(RECHARGE_INCREMENT);
            amountToRecharge = amountToRecharge.subtract(RECHARGE_INCREMENT);
        }
        account.deposit(rechargeBalance);
    }
}
