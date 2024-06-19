package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.model.PendingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PendingTransactionRepository extends JpaRepository<PendingTransaction, Long> {
    List<PendingTransaction> findByExpiresAtBeforeAndRemindedFalse(LocalDateTime now);
    List<PendingTransaction> findByExpiresAtBefore(LocalDateTime now);
}
