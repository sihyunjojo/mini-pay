package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.exception.InsufficientBalanceException;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingService {

    private final AccountRepository accountRepository;
    private final TransferService transferService;

    @Transactional
    public void processRecurringSavings() {
        List<Account> recurringAccounts = accountRepository.findAllRecurringSavingsAccounts();
        for (Account account : recurringAccounts) {
            try {
                transferService.depositIntoSavingsAccount(account, account.getSavingBalance());
            } catch (InsufficientBalanceException e) {
                throw new InsufficientBalanceException();
            }
        }
    }
}