package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.generator.RandomGenerator;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.c4marathon.assignment.util.AccountType.ADMIN_ACCOUNT_ID;

@Service
@RequiredArgsConstructor
public class CalculateService {
    private final RandomGenerator randomGenerator;
    private final AccountValidator accountValidator;
    private final TransferService transferService;

    @Transactional
    public void calculateBalanceEqually(Long fromAccountId, List<Long> toAccountIds, BigDecimal totalBalance) {
        accountValidator.validateDecimalBalance(totalBalance);

        Map<Long, BigDecimal> equalBalances = distributeEqually(toAccountIds, totalBalance);

        for (Map.Entry<Long, BigDecimal> entry : equalBalances.entrySet()) {
            transferService.transferMoney(fromAccountId, entry.getKey(), entry.getValue());
        }
    }

    private Map<Long, BigDecimal> distributeEqually(List<Long> toAccountIds, BigDecimal totalBalance) {
        int numberOfAccounts = toAccountIds.size();
        int totalBalanceInt = totalBalance.intValueExact();
        int perAccountBalanceInt = totalBalanceInt / numberOfAccounts;
        int remainderInt = totalBalanceInt % numberOfAccounts;

        Map<Long, BigDecimal> accountBalances = new HashMap<>();
        for (Long toAccountId : toAccountIds) {
            BigDecimal amountToSend = BigDecimal.valueOf(perAccountBalanceInt);
            accountBalances.put(toAccountId, amountToSend);
        }

        if (remainderInt > 0) {
            BigDecimal remainder = BigDecimal.valueOf(remainderInt);
            accountBalances.put(ADMIN_ACCOUNT_ID, remainder);
        }

        return accountBalances;
    }


    @Transactional
    public void calculateBalanceRandomly(Long fromAccountId, List<Long> toAccountIds, BigDecimal totalBalance) {
        Map<Long, BigDecimal> randomBalanceMap = randomGenerator.generateRandomBalances(toAccountIds, totalBalance);
        for (Map.Entry<Long, BigDecimal> entry : randomBalanceMap.entrySet()) {
            transferService.transferMoney(fromAccountId, entry.getKey(), entry.getValue());
        }
    }
}
