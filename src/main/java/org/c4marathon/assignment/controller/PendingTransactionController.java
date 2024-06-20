package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.PendingTransaction;
import org.c4marathon.assignment.repository.PendingTransactionRepository;
import org.c4marathon.assignment.service.PendingTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/pending")
@RequiredArgsConstructor
public class PendingTransactionController {

    private final PendingTransactionService pendingTransactionService;
    private final PendingTransactionRepository pendingTransactionRepository;

    @GetMapping("/from/{fromAccountId}")
    public ResponseEntity<?> getPendingTransactionsByFrom(@PathVariable Long fromAccountId) {
        return ResponseEntity.ok().body(pendingTransactionService.getPendingTransactionsByFrom(fromAccountId));
    }

    @GetMapping("/to/{fromAccountId}")
    public ResponseEntity<?> getPendingTransactionsByTo(@PathVariable Long fromAccountId) {
        return ResponseEntity.ok().body(pendingTransactionService.getPendingTransactionsByTo(fromAccountId));
    }

    //Pending 상태의 금액은 잔고에서 차감한 것으로 간주합니다. 즉, 잔고가 30,000원인 상태에서 20,000원이 Pending 상태라면, 추가 송금이 가능한 금액은 10,000원 입니다.
    @PostMapping("/{fromAccountId}/{toAccountId}/{amount}")
    public ResponseEntity<?> createPendingTransaction(@PathVariable Long fromAccountId,
                                                   @PathVariable Long toAccountId,
                                                   @PathVariable BigDecimal amount) {
        PendingTransaction pendingTransaction = pendingTransactionService.createPendingTransaction(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok().body(pendingTransaction);
    }

    // Pending 상태의 잔액은 송금을 취소할 경우, 원래 금액으로 수령할 수 있습니다.
    @DeleteMapping("/cancel/{pendingTransactionId}")
    public ResponseEntity<?> cancelPendingTransaction(@PathVariable Long pendingTransactionId) {
        pendingTransactionService.cancelPendingTransaction(pendingTransactionId);
        return ResponseEntity.ok().build();
    }
}
