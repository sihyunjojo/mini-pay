package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Transaction;
import org.c4marathon.assignment.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.c4marathon.assignment.util.AccountType.DAILY_CHARGING_LIMIT;

@Service
@RequiredArgsConstructor
public class TransactionalService {
    private TransactionRepository transactionRepository;

    // 로그를 남기는 트랜잭션은 중요하지 않으므로 트랜잭션을 중요 트랜잭션과 분리해서 작동.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransaction(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
