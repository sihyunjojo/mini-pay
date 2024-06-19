package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.model.PendingTransaction;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.PendingTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingTransactionService {
    private final AccountRepository accountRepository;
    private final PendingTransactionRepository pendingTransactionRepository;
    private final TransactionalService transactionalService;
    private final TransferService transferService;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPendingTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completePendingTransaction(Long pendingTransactionId) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        transferService.executeTransfer(pendingTransaction.getFromAccount(), pendingTransaction.getToAccount(), pendingTransaction.getAmount());
        pendingTransactionRepository.delete(pendingTransaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelPendingTransaction(Long pendingTransactionId) {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findById(pendingTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found"));

        Account fromAccount = pendingTransaction.getFromAccount();
        fromAccount.deposit(pendingTransaction.getAmount());

        pendingTransactionRepository.delete(pendingTransaction);
    }

    public void sendReminders() {
        List<PendingTransaction> pendingTransactions = pendingTransactionRepository.findByExpiresAtBeforeAndRemindedFalse(LocalDateTime.now().plusHours(24));
        for (PendingTransaction pendingTransaction : pendingTransactions) {
            // send reminder notification to pendingTransaction.getToAccount().getUser()
            pendingTransaction.setReminded(true);
            pendingTransactionRepository.save(pendingTransaction);
        }
    }

    @Transactional
    public void expirePendingTransactions() {
        List<PendingTransaction> pendingTransactions = pendingTransactionRepository.findByExpiresAtBefore(LocalDateTime.now());
        for (PendingTransaction pendingTransaction : pendingTransactions) {
            cancelPendingTransaction(pendingTransaction.getId());
        }
    }
}
