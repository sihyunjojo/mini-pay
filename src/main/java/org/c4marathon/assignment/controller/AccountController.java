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


    // 추가적으로, "적금 계좌" 를 생성할 수 있습니다.
    @PostMapping("/savings/{userId}")
    public ResponseEntity<Account> createSavingsAccount(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(accountService.createSavingsAccount(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 메인 게좌에 충전
    // 이 계좌는 외부 계좌에서 돈을 가져오는 기능이 주 기능이므로, 금액 추가가 가능합니다.
    @PostMapping("/addMoney/{accountId}")
    public ResponseEntity<Account> addMoney(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.findById(accountId)
                .map(account -> ResponseEntity.ok(transferService.addMoneyToMainAccount(account, amount)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 적금계좌가 메인 계좌로 부터 돈을 가져옴.
    // 이 계좌는 메인 계좌에서 돈을 인출할 수 있으며, 메인 계좌의 돈이 없으면 인출할 수 없습니다.
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<Account> addMoneyIntoSavingAccount(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Optional<Account> accountOptional = accountService.findById(accountId);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            transferService.depositIntoSavingsAccount(account, amount);
            return ResponseEntity.ok(account);
        }
        return ResponseEntity.notFound().build(); // Return HTTP 404 Not Found if account not found
    }
}