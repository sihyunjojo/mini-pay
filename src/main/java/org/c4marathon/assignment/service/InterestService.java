package org.c4marathon.assignment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// 카카오뱅크 한달적금에서 하루라도 입금을 하지 않으면 다음과 같이 금리와 이자에 영향을 미칩니다:
//매일 입금할 때마다 0.1%p의 우대금리가 제공됩니다. 따라서 하루라도 입금하지 않으면 그 날의 우대금리 0.1%p를 받지 못합니다.
//5회, 10회, 15회, 20회, 25회, 31회(만기) 입금 시 추가 보너스 우대금리가 제공됩니다. 해당 회차에 입금하지 않으면 그 회차의 보너스 우대금리를 받지 못합니다.
//최종 만기 금리는 기본금리 연 1.5%에 매일 입금한 날의 우대금리 0.1%p와 보너스 우대금리를 합산하여 최대 연 7%까지 받을 수 있습니다. 하루라도 입금하지 않으면 최고 금리를 받지 못합니다.
//만기 해지 시 입금한 횟수만큼 받은 우대금리는 그대로 적용되지만, 입금하지 않은 날의 우대금리는 제외됩니다.
//따라서 카카오뱅크 한달적금에서 하루라도 입금을 하지 ㅂ않으면 그 날의 우대금리와 보너스 우대금리를 받지 못해 최종 이자수령액이 줄어들게 됩니다.
@Service
@RequiredArgsConstructor
public class InterestService {

    private final AccountRepository accountRepository;

    @Transactional
    public void applyInterest() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (account.isRecurringSavings()) {
                applyRecurringSavingsInterest(account);
            } else if (account.isFlexibleSavings()) {
                applyFlexibleSavingsInterest(account);
            }
        }
    }


    public void applyRecurringSavingsInterest(Account account) {
        BigDecimal interestRate = new BigDecimal("0.05");
        BigDecimal dailyInterest = account.getBalance().multiply(interestRate).divide(new BigDecimal("365"), RoundingMode.FLOOR);
        account.deposit(dailyInterest);
    }

    public void applyFlexibleSavingsInterest(Account account) {
        BigDecimal interestRate = new BigDecimal("0.03");
        BigDecimal dailyInterest = account.getBalance().multiply(interestRate).divide(new BigDecimal("365"), RoundingMode.FLOOR);
        account.deposit(dailyInterest);
    }
}
