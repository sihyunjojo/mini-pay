package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.exception.InsufficientBalanceException;
import org.c4marathon.assignment.generator.RandomGenerator;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.util.AccountType;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.c4marathon.assignment.util.AccountType.*;

// MySQL 의 기본 격리 레벨은 REPEATABLE READ 이다.
// 디폴트 설정은 기본 데이터베이스의 기본 격리 수준이 사용됨을 의미합니다.

// 값이 수정 가능한 컬럼을 접근 할때는 기본적으로 REPEATABLE READ를 적용하는게 좋아보이고
// SERIALIZABLE까지는 적용하지 않는 이유는 트랜잭션이 접근하는 컬럼에서 다른 컬럼에 의해서 영향을 받아서 결과가 나오는 작업이 없어서 phantom read는 신경쓰지 않아도 된다고 생각.

// 그치만 create와 같은 초기의 중요한 작업은 사용자의 주요 정보가 들어가므로 보안을 생각하였고
// transfe로직의 경우에는 두 개의 계좌에 접근하는 작업에서 서로 다른 컬럼의 영향을 서로 받는 경우에서 최고 수준의 정책을 적용함.
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionalService transactionalService;
    private final RechargeService rechargeService;
    private final AccountValidator accountValidator;
    private final RandomGenerator randomGenerator;

    // 계정이 생성되는 것은 중요한 처리이고, 다른 작업이 동시에 진행되면 안된다고 생각해서 이러한 정책 고수.
    // 오히려 다른 것들이 충돌이 될 이유가 없으니까 더 강력한 보안을 적용해도 성능에 큰 문제를 미치지 않으므로, 이러한 정책 고수.
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Account createMainAccount(User user) {
        return createAccount(user, MAIN);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Account createSavingsAccount(User user) {
        return createAccount(user, SAVINGS);
    }



    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ) // Ensuring consistent reads
    public Optional<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void resetDailyLimits() {
        List<Account> accounts = accountRepository.findAll();
        accounts.forEach(account -> account.setTotalChargedInPeriod(BigDecimal.ZERO));
//        accountRepository.saveAll(accounts);
        // ?
    }

    private Account createAccount(User user, String type) {
        Account account = new Account();
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        return accountRepository.save(account);
    }
}