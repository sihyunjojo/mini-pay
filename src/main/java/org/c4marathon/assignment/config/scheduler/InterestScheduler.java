package org.c4marathon.assignment.config.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Account;
import org.c4marathon.assignment.service.InterestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InterestScheduler {

    public final InterestService interestService;

    // 초 분 시 일 월 요일
    @Transactional
    @Scheduled(cron = "30 * * * * ?") // 매일 오전 4시에 실행
    public void dailyApplyInterest(){
        // 모든 계좌를 가져와서 이자를 계산하고 적용
        interestService.applyInterest();
    }
}
