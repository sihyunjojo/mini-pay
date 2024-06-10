package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping("/main/{userId}")
    public ResponseEntity<Account> createMainAccount(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(accountService.createMainAccount(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/savings/{userId}")
    public ResponseEntity<Account> createSavingsAccount(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(accountService.createSavingsAccount(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/addMoney/{accountId}")
    public ResponseEntity<Account> addMoney(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.findById(accountId)
                .map(account -> ResponseEntity.ok(accountService.addMoneyToMainAccount(account, amount)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<Object> deposit(@PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.findById(accountId)
                .map(account -> {
                    accountService.depositIntoSavingsAccount(account, amount);
                    return ResponseEntity.ok().build(); // Return HTTP 200 OK with no content
                })
                .orElse(ResponseEntity.notFound().build());
    }
}