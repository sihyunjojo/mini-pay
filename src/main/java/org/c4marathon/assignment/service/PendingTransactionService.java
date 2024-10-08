package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.model.PendingTransaction;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.PendingTransactionRepository;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.c4marathon.assignment.util.AccountType.REMIND_STANDARD_TIME;

@Service
@RequiredArgsConstructor
public class PendingTransactionService {
    private final AccountRepository accountRepository;
    private final PendingTransactionRepository pendingTransactionRepository;

    private final TransactionalService transactionalService;
    private final AccountService accountService;

    private final AccountValidator accountValidator;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PendingTransaction createPendingTransaction(long fromAccountId, long toAccountId, BigDecimal amount) {
        Optional<Account> optionalFromAccount = accountService.findById(fromAccountId);
        Optional<Account> optionalAccountToAccount = accountService.findById(toAccountId);
        Account fromAccount = accountValidator.validateMainAccount(optionalFromAccount);
        Account toAccount = accountValidator.validateMainAccount(optionalAccountToAccount);

        accountValidator.validateWithdrawBalance(fromAccount, amount);
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        PendingTransaction pendingTransaction = new PendingTransaction();
        pendingTransaction.setFromAccount(fromAccount);
        pendingTransaction.setToAccount(toAccount);
        pendingTransaction.setAmount(amount);
        pendingTransaction.setCreatedAt(LocalDateTime.now());
        pendingTransaction.setExpiresAt(LocalDateTime.now().plusHours(72));
        pendingTransaction.setReminded(false);

        pendingTransactionRepository.save(pendingTransaction);
        transactionalService.logTransaction(fromAccount.getId(), toAccount.getId(), amount);

        return pendingTransaction;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completePendingTransaction(Long pendingTransactionId, String fromAccountAlias) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        pendingTransactionRepository.delete(pendingTransaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completePendingTransaction(Long pendingTransactionId) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        pendingTransactionRepository.delete(pendingTransaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelPendingTransaction(Long pendingTransactionId) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        Account fromAccount = pendingTransaction.getFromAccount();
        fromAccount.setBalance(fromAccount.getBalance().add(pendingTransaction.getAmount()));

        pendingTransactionRepository.delete(pendingTransaction);
    }

    public void sendReminders() {
        List<PendingTransaction> pendingTransactions = pendingTransactionRepository.findByExpiresAtBeforeAndRemindedFalse(REMIND_STANDARD_TIME);
        for (PendingTransaction pendingTransaction : pendingTransactions) {
            pendingTransaction.setReminded(true);
        }
    }

    @Transactional
    public void expirePendingTransactions() {
        List<PendingTransaction> pendingTransactions = pendingTransactionRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (PendingTransaction pendingTransaction : pendingTransactions) {
            cancelPendingTransaction(pendingTransaction.getId());
        }
    }

    public List<PendingTransaction> getPendingTransactionsByFrom(Long fromAccountId) {
        return pendingTransactionRepository.findByFromAccountId(fromAccountId);
    }

    public List<PendingTransaction> getPendingTransactionsByTo(Long toAccountId) {
        return pendingTransactionRepository.findByToAccountId(toAccountId);
    }

    public Map<String,Object> getTransferInformation(Long pendingTransactionId) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        return Map.of(
                "fromAccount", pendingTransaction.getFromAccount(),
                "toAccount", pendingTransaction.getToAccount(),
                "amount", pendingTransaction.getAmount()
        );
    }
}
