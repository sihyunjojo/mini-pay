package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        Account mainAccount = accountService.createMainAccount(user);
        createdUser.addAccount(mainAccount);

        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        System.out.println(id);
        Optional<User> user = userService.getUserById(id);
        System.out.println(user);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

}