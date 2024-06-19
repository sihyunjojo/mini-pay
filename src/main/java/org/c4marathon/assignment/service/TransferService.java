package org.c4marathon.assignment.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.exception.InsufficientBalanceException;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.validate.AccountValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final RechargeService rechargeService;
    private final TransactionalService transactionalService;

    // 동일한 트랜잭션 내에서 계정 잔액을 여러 번 확인하면 동일한 값을 반환하여 읽기 사이에 다른 트랜잭션에 의해 잔액이 수정될 수 있는 문제를 방지합니다.
    @Transactional(isolation = Isolation.REPEATABLE_READ) // Ensuring consistent reads
    public Account addMoneyToMainAccount(Account account, BigDecimal amount) {
        accountValidator.validateAccountBalance(account, amount);

        accountValidator.validateMainAccount(account);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        transactionalService.logTransaction(account.getId(), null, amount);
        return account;
    }

    // 읽기 작업 결과에 영향을 미칠 수 있는 다른 트랜잭션에 의해 새 행이 삽입되는 경우 해당 행이 동일한 트랜잭션 내에서 표시되지 않도록 보장하여 불일치를 방지합니다.
    @Transactional(isolation = Isolation.REPEATABLE_READ) // Ensuring consistent reads
    public void depositIntoSavingsAccount(Account account, BigDecimal amount) {
        accountValidator.validateAccountBalance(account, amount);

        accountValidator.validateSavingsAccount(account);

        Account mainAccount = accountRepository.findMainAccountByUserId(account.getUser().getId()).orElse(null);
        accountValidator.validateMainAccount(mainAccount);

        executeTransfer(mainAccount, account, amount);
    }

    // write가 일어나는 트랜잭션을 분리하여서 read만 일어나는 이 로직에서는 기본 정책인 REPEATABLE_READ 정책.
    @Transactional()
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {
        Account fromAccount = accountRepository.findMainAccountByUserId(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
        Account toAccount = accountRepository.findMainAccountByUserId(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient account not found"));

        // 트랜잭션 분리하여서 짧게 가져가기
        // 만약, 잔액이 부족하다면 10,000원 단위로 충전한 후 송금이 이루어진다.
        try {
            accountValidator.validateTransferAccounts(fromAccount, toAccount, amount);
        } catch (InsufficientBalanceException e){
            rechargeService.rechargeIncrements(amount, fromAccount);
        }

        executeTransfer(fromAccount, toAccount, amount);
    }

    // 위와는 다른 새로운 트랜잭션을 실행한다.
    // 엄격한 데이터 일관성을 보장하기 위해서 read와 write 분리

    // 엄격한 격리: 여러 읽기 및 쓰기 작업이 포함된 전체 전송 프로세스가 다른 동시 트랜잭션과 격리되도록 보장합니다.
    // 불일치 방지: 이체 중에 다른 거래가 잔액에 영향을 미치지 않도록 하여 이중 지출이나 잘못된 잔액과 같은 문제를 방지합니다.

    // 같은 레벨의 SERIALIZABLE임에도 최대한 트랜잭션을 짧게 가져갈 수 있도록 구상해 봅시다.
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        fromAccount.deposit(fromAccount.getBalance().subtract(amount));
        toAccount.withdraw(toAccount.getBalance().add(amount));

        // 트랜잭션 컨텍스트에서 업데이트를 수행할 때 버전 필드가 자동으로 확인됩니다.
        accountRepository.save(fromAccount); // 여기에서 버전을 확인하고 증가시킵니다.
        accountRepository.save(toAccount); // 여기에서 버전을 확인하고 증가시킵니다.

        transactionalService.logTransaction(fromAccount.getId(), toAccount.getId(), amount);
    }

}
