package org.c4marathon.assignment.validate;

import org.c4marathon.assignment.exception.InsufficientBalanceException;
import org.c4marathon.assignment.model.Account;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.c4marathon.assignment.util.AccountType.*;

@Component
public class AccountValidator {

    public void validateWithdrawBalance(Account account, BigDecimal amount) {
        validateDecimalBalance(amount);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    public void validateAccountBalance(Account account, BigDecimal amount) {
        validateDecimalBalance(amount);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        // 추가로 보안적으로 더 관여할 사항이 잇을거 같지만 뒤로 미뤄둠 ?
        if (account.getId() == ADMIN_ACCOUNT_ID) return;

        BigDecimal newChargeLimit = account.getTotalChargedInPeriod().add(amount);
        if (newChargeLimit.compareTo(DAILY_CHARGING_LIMIT) > 0) {
            throw new IllegalArgumentException("Daily limit exceeded");
        }
    }


    public void validateSavingsAccount(Account savingAaccount) {
        if (savingAaccount == null) {
            throw new IllegalArgumentException("Main account not found");
        }
        if (!savingAaccount.getType().equals(SAVINGS)) {
            throw new IllegalArgumentException("This is not a savings account");
        }

    }

    public Account validateMainAccount(Optional<Account> mainAccount) {
        if (mainAccount.isEmpty()) {
            throw new IllegalArgumentException("Main account not found");
        }
        if (!mainAccount.get().getType().equals(MAIN)) {
            throw new IllegalArgumentException("This is not a savings account");
        }
        return mainAccount.get();
    }

    public List<Account> validateMainAccounts(List<Optional<Account>> accounts) {
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("Main account not found");
        }

        List<Account> mainAccounts = new ArrayList<>();
        for (Optional<Account> account : accounts) {
            if (account.isEmpty()) {
                throw new IllegalArgumentException("Main account not found");
            }
            if (!account.get().getType().equals(MAIN)) {
                throw new IllegalArgumentException("This is not a savings account");
            }

            mainAccounts.add(account.get());
        }
        return mainAccounts;
    }

    public void validateMainAccount(Account mainAccount) {
        if (mainAccount == null) {
            throw new IllegalArgumentException("Main account not found");
        }
        if (!mainAccount.getType().equals(MAIN)) {
            throw new IllegalArgumentException("This is not a savings account");
        }
    }


    public void validateTransferAccounts(Account fromAccount, Account toAccount, BigDecimal amount) {
        validateDecimalBalance(amount);
        validateAccountBalance(fromAccount, amount);
        validateMainAccount(fromAccount);
        validateSavingsAccount(toAccount);
    }

    // 소수인지 판별 하는 검증기
    public void validateDecimalBalance(BigDecimal totalBalance) {
        if (totalBalance.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Total balance must be an integer value without decimal points.");
        }
    }
}
