package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.service.PendingTransactionService;
import org.c4marathon.assignment.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final PendingTransactionService pendingTransactionService;

    // 친구의 메인 계좌로 송금
    // 메인 계좌는 사람당 하나 씩만 있으므로 userId를 가져옴.
    // 송금 기능을 추가합시다.
    // 친구의 메인 계좌로 송금이 가능합니다.
    @PostMapping("/account/{fromAccountId}/{toAccountId}/{amount}")
    public ResponseEntity<Void> transferMoneyByAccountId(@PathVariable Long fromAccountId,
                                              @PathVariable Long toAccountId,
                                              @PathVariable BigDecimal amount) {
        transferService.transferMoneyByAccountId(fromAccountId, toAccountId, amount);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/account/{fromAccountId}/{toAccountId}/{amount}/{fromAccountAlias}")
    public ResponseEntity<Void> transferMoneyByAccountId(@PathVariable Long fromAccountId,
                                                         @PathVariable Long toAccountId,
                                                         @PathVariable BigDecimal amount,
                                                        @PathVariable String fromAccountAlias) {
        transferService.transferMoneyByAccountId(fromAccountId, toAccountId, amount, fromAccountAlias);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{fromUserId}/{toUserId}/{amount}")
    public ResponseEntity<Void> transferMoneyByUserId(@PathVariable Long fromUserId,
                                              @PathVariable Long toUserId,
                                              @PathVariable BigDecimal amount) {
        transferService.transferMoney(fromUserId, toUserId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{fromUserId}/{toUserId}/{amount}/{fromAccountAlias}")
    public ResponseEntity<Void> transferMoney(@PathVariable Long fromUserId,
                                              @PathVariable Long toUserId,
                                              @PathVariable BigDecimal amount,
                                              @PathVariable String fromAccountAlias) {
        transferService.transferMoney(fromUserId, toUserId, amount, fromAccountAlias);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pending/{pendingTransactionId}/{fromAccountAlias}")
    public ResponseEntity<Void> pendingTransferMoney(@PathVariable Long pendingTransactionId,
                                                     @PathVariable String fromAccountAlias) {
        transferService.pendingTransferMoney(pendingTransactionId, fromAccountAlias);
        pendingTransactionService.completePendingTransaction(pendingTransactionId, fromAccountAlias);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pending/{pendingTransactionId}")
    public ResponseEntity<Void> pendingTransferMoney(@PathVariable Long pendingTransactionId) {
        transferService.pendingTransferMoney(pendingTransactionId);
        pendingTransactionService.completePendingTransaction(pendingTransactionId);
        return ResponseEntity.ok().build();
    }

}
