package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.TransferService;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final TransferService transferService;
    private final UserService userService;


    // 적금 계좌 만들기
    @PostMapping("/savings/{userId}")
    public ResponseEntity<Account> createSavingsAccount(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(accountService.createSavingsAccount(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 메인 게좌에 충전
    @PostMapping("/addMoney/{accountId}")
    public ResponseEntity<Account> addMoney(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.findById(accountId)
                .map(account -> ResponseEntity.ok(transferService.addMoneyToMainAccount(account, amount)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 적금계좌가 메인 계좌로 부터 돈을 가져옴.
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<Account> deposit(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Optional<Account> accountOptional = accountService.findById(accountId);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            transferService.depositIntoSavingsAccount(account, amount);
            // Retrieve the updated account after the deposit
            Optional<Account> updatedAccountOptional = accountService.findById(accountId);
            if (updatedAccountOptional.isPresent()) {
                return ResponseEntity.ok(updatedAccountOptional.get()); // Return HTTP 200 OK with the updated account
            }
        }
        return ResponseEntity.notFound().build(); // Return HTTP 404 Not Found if account not found
    }


    // 친구의 메인 계좌로 송금
    // 메인 계좌는 사람당 하나 씩만 있으므로 userId를 가져옴.
    @PostMapping("/transfer/{fromUserId}/{toUserId}")
    public ResponseEntity<Void> transferMoney(@PathVariable Long fromUserId,
                                              @PathVariable Long toUserId,
                                              @RequestParam BigDecimal amount) {
        transferService.transferMoney(fromUserId, toUserId, amount);
        return ResponseEntity.ok().build();
    }
}