package org.c4marathon.assignment.service;

import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.c4marathon.assignment.util.AccountType.*;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createMainAccount(User user) {
        Account account = new Account();
        account.setType(MAIN);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        return accountRepository.save(account);
    }

    public Account createSavingsAccount(User user) {
        Account account = new Account();
        account.setType(SAVINGS);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        return accountRepository.save(account);
    }

    @Transactional
    public Account addMoneyToMainAccount(Account account, BigDecimal amount) {
        if (amount.compareTo(PARTICIPATION_LIMIT) > 0) {
            throw new IllegalArgumentException("Daily limit exceeded");
        }
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    // @Transactional(isolation = Isolation.READ_COMMITTED)를 사용하면 트랜잭션이 커밋된 데이터만 읽을 수 있도록 보장하여
    // 더티 읽기(dirty read)를 방지하는 READ_COMMITTED 격리 수준을 사용하도록 트랜잭션을 구성합니다.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void depositIntoSavingsAccount(Account account, BigDecimal amount) {
        if (!account.getType().equals(SAVINGS)) {
            throw new IllegalArgumentException("This is not a savings account");
        }

        Account mainAccount = findMainAccountByUserId(account.getUser().getId()).orElse(null);

        if (mainAccount == null) {
            throw new IllegalArgumentException("Main account not found");
        }

        if (mainAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        depositIntoAccount(mainAccount, account, amount);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findMainAccountByUserId(Long userId) {
        return accountRepository.findMainAccountByUserId(userId);
    }

    public void depositIntoAccount(Account account, Account mainAccount, BigDecimal amount) {
        account.deposit(amount);
        mainAccount.withdraw(amount);

        accountRepository.save(account);
        accountRepository.save(mainAccount);
    }
}