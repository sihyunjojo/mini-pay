package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
//    List<Transaction> findByFromAccountIdAndTimestampBetween(Long fromAccountId, LocalDateTime start, LocalDateTime end);
//    List<Transaction> findByToAccountIdAndTimestampBetween(Long toAccountId, LocalDateTime start, LocalDateTime end);
//    List<Transaction> findByFromAccountIdOrToAccountIdAndTimestampBetween(Long fromAccountId, Long toAccountId, LocalDateTime start, LocalDateTime end);
}
