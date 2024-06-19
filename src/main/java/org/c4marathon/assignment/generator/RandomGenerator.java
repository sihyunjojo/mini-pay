package org.c4marathon.assignment.generator;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class RandomGenerator {

    private final AccountValidator accountValidator;
    // ThreadLocalRandom 클래스는 각 스레드가 독립된 난수 생성기를 사용하여 멀티스레드 환경에서 더 나은 성능을 제공합니다.
    private static final Random defaultRandom = ThreadLocalRandom.current();

    public Map<Long, BigDecimal> generateRandomBalances(List<Long> toAccountIds, BigDecimal totalBalance) {
        accountValidator.validateDecimalBalance(totalBalance);
        validateAccountIds(toAccountIds);

        int numberOfAccounts = toAccountIds.size();
        Map<Long, BigDecimal> accountBalances = new HashMap<>();
        int remainingBalanceInt = totalBalance.intValueExact();

        // Shuffle account IDs to ensure fairness
        Collections.shuffle(toAccountIds, defaultRandom);

        // Generate random weights
        int[] randomWeights = generateRandomWeights(numberOfAccounts);

        // Distribute balances based on weights
        int allocatedBalance = distributeBalances(toAccountIds, accountBalances, randomWeights, remainingBalanceInt);

        // Assign remaining balance to the last account
        assignRemainingBalance(toAccountIds, accountBalances, remainingBalanceInt, allocatedBalance);

        return accountBalances;
    }

    private int[] generateRandomWeights(int numberOfAccounts) {
        int[] randomWeights = new int[numberOfAccounts];

        for (int i = 0; i < numberOfAccounts; i++) {
            randomWeights[i] = defaultRandom.nextInt(100) + 1; // Generate random weight between 1 and 100
        }
        return randomWeights;
    }

    private int distributeBalances(List<Long> toAccountIds, Map<Long, BigDecimal> accountBalances, int[] randomWeights, int remainingBalanceInt) {
        int totalWeight = Arrays.stream(randomWeights).sum();
        int allocatedBalance = 0;

        for (int i = 0; i < toAccountIds.size() - 1; i++) {
            int weight = randomWeights[i];
            int balance = remainingBalanceInt * weight / totalWeight;
            allocatedBalance += balance;
            accountBalances.put(toAccountIds.get(i), BigDecimal.valueOf(balance));
        }
        return allocatedBalance;
    }

    private void assignRemainingBalance(List<Long> toAccountIds, Map<Long, BigDecimal> accountBalances, int remainingBalanceInt, int allocatedBalance) {
        int lastAccountIdIndex = toAccountIds.size() - 1;
        int lastBalance = remainingBalanceInt - allocatedBalance;
        accountBalances.put(toAccountIds.get(lastAccountIdIndex), BigDecimal.valueOf(lastBalance));
    }

    private void validateAccountIds(List<Long> toAccountIds) {
        if (toAccountIds == null || toAccountIds.isEmpty()) {
            throw new IllegalArgumentException("Account IDs list cannot be null or empty.");
        }
    }
}
