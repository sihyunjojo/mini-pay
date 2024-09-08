package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Transaction;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @RequiredArgsConstructor가 뭔가 이상한 바뀐거같음??
@Service
public class TransactionalService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Autowired
    public TransactionalService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    // 로그를 남기는 트랜잭션은 중요하지 않으므로 트랜잭션을 중요 트랜잭션과 분리해서 작동.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransaction(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        if (fromAccountId == null) {
            transaction.setFromAccountId(0L);
        } else {
            transaction.setFromAccountId(fromAccountId);
        }

        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        if (fromAccountId == null || fromAccountId == 0L) {
            transaction.setFromAccountAlias("System");
        } else {
            System.out.println(fromAccountId);
            User user = userService.findUserByAccountId(fromAccountId);
            transaction.setFromAccountAlias(user.getNickname());
        }

        System.out.println(transaction);

        transactionRepository.save(transaction);
    }

    public void logTransaction(Long fromAccountId, Long toAccountId, BigDecimal amount, String fromAccountAlias) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        if (fromAccountAlias.isEmpty() || fromAccountAlias.isBlank()) {
            transaction.setFromAccountAlias(userService.findById(fromAccountId).get().getNickname());
        } else {
            transaction.setFromAccountAlias(fromAccountAlias);
        }
        transactionRepository.save(transaction);
    }
}
