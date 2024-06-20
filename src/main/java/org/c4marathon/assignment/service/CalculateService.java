package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.generator.RandomGenerator;
import org.c4marathon.assignment.model.*;
import org.c4marathon.assignment.repository.AccountToPersonalCalculateRepository;
import org.c4marathon.assignment.repository.CalculateRepository;
import org.c4marathon.assignment.repository.PersonalCalculateRepository;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.c4marathon.assignment.util.AccountType.*;

@Service
@RequiredArgsConstructor
public class CalculateService {
    private final RandomGenerator randomGenerator;
    private final AccountValidator accountValidator;
    
    private final AccountService accountService;
    
    private final CalculateRepository calculateRepository;
    private final PersonalCalculateRepository personalCalculateRepository;
    private final AccountToPersonalCalculateRepository accountToPersonalCalculateRepository;

    @Transactional
    public Calculate calculateBalanceEqually(Long fromAccountId, List<Long> toAccountIds, BigDecimal totalBalance) {
        accountValidator.validateDecimalBalance(totalBalance);

        Calculate calculate = makeBasicCalculate(fromAccountId, totalBalance, EQUALLY_CALCULATE);

        Map<Long, BigDecimal> equalBalances = distributeEqually(toAccountIds, totalBalance);
        return updateCalculate(equalBalances, calculate);
    }

    @Transactional
    public Calculate calculateBalanceEquallyByUser(User fromUser, List<User> toUsers, BigDecimal totalBalance) {
        Long fromAccountId = userToAccountId(fromUser);
        List<Long> toAccountIds = usersToAccountIds(toUsers);

        return calculateBalanceEqually(fromAccountId, toAccountIds, totalBalance);
    }

    private Map<Long, BigDecimal> distributeEqually(List<Long> toAccountIds, BigDecimal totalBalance) {
        int numberOfAccounts = toAccountIds.size();
        int totalBalanceInt = totalBalance.intValueExact();
        int perAccountBalanceInt = totalBalanceInt / numberOfAccounts;
        int remainderInt = totalBalanceInt % numberOfAccounts;

        return makeEquallyCalculate(toAccountIds, perAccountBalanceInt, remainderInt);
    }

    private static Map<Long, BigDecimal> makeEquallyCalculate(List<Long> toAccountIds, int perAccountBalanceInt, int remainderInt) {
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
    public Calculate calculateBalanceRandomly(Long fromAccountId, List<Long> toAccountIds, BigDecimal totalBalance) {
        accountValidator.validateDecimalBalance(totalBalance);
        Map<Long, BigDecimal> randomBalanceMap = randomGenerator.generateRandomBalances(toAccountIds, totalBalance);

        Calculate calculate = makeBasicCalculate(fromAccountId, totalBalance, RANDOM_CALCULATE);

        return updateCalculate(randomBalanceMap, calculate);

    }



    @Transactional
    public Calculate calculateBalanceRandomlyByUser(User fromUser, List<User> toUsers, BigDecimal totalBalance) {
        Long fromAccountId = userToAccountId(fromUser);
        List<Long> toAccountIds = usersToAccountIds(toUsers);

        return calculateBalanceRandomly(fromAccountId, toAccountIds, totalBalance);
    }

    private Calculate makeBasicCalculate(Long fromAccountId, BigDecimal totalBalance, String type) {
        Optional<Account> fromAccount = accountService.findById(fromAccountId);
        Account fromMainAccount = accountValidator.validateMainAccount(fromAccount);

        Calculate calculate = new Calculate();
        calculate.setTotalAmount(totalBalance);
        calculate.setType(type);
        calculate.setFromAccount(fromMainAccount);

        return calculateRepository.save(calculate);
    }

    private Calculate updateCalculate(Map<Long, BigDecimal> balanceMapByAccountId, Calculate calculate) {
        List<PersonalCalculate> personalCalculates = getPersonalCalculates(balanceMapByAccountId, calculate);
        calculate.setPersonalCalculate(personalCalculates);

        List<Long> accountIdList = balanceMapByAccountId.keySet()
                .stream()
                .toList();
        int count = 0;

        List<AccountToPersonalCalculate> accountToPersonalCalculates = personalCalculates.stream()
                .map(personalCalculate -> {
                    AccountToPersonalCalculate accountToPersonalCalculate = new AccountToPersonalCalculate();
                    accountToPersonalCalculate.setPersonalCalculate(personalCalculate);
                    accountToPersonalCalculate.setToAccount(accountService.findById(accountIdList.get(count)).get());
                    return accountToPersonalCalculate;
                })
                .toList();

        accountToPersonalCalculateRepository.saveAll(accountToPersonalCalculates);
        return calculate;
    }

    private List<PersonalCalculate> getPersonalCalculates(Map<Long, BigDecimal> balanceMapByAccountId, Calculate calculate) {
        List<PersonalCalculate> personalCalculates = balanceMapByAccountId.values().stream()
                .map(balance -> {
                    PersonalCalculate personalCalculate = new PersonalCalculate();
                    personalCalculate.setBalance(balance);
                    personalCalculate.setCalculate(calculate);
                    return personalCalculate;
                })
                .toList();

        personalCalculateRepository.saveAll(personalCalculates);
        return personalCalculates;
    }


    private Long userToAccountId(User user) {
        Optional<Account> fromAccountId = accountService.findById(user.getId());
        Account account = accountValidator.validateMainAccount(fromAccountId);

        return account.getId();
    }

    private List<Long> usersToAccountIds(List<User> users) {
        List<Optional<Account>> toAccountIds = accountService.findAccountsByUserId(
                users.stream()
                        .map(User::getId)
                        .toList()
        );

        List<Account> validatedToMainAccounts = accountValidator.validateMainAccounts(toAccountIds);

        return validatedToMainAccounts.stream()
                .map(Account::getId)
                .toList();
    }
}
