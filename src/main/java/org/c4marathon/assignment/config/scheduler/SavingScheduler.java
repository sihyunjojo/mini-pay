package org.c4marathon.assignment.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.service.SavingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingScheduler {

    private final SavingService savingService;

    @Scheduled(cron = "0 0 8 * * ?") // 매일 오전 8시에 실행
    public void dailyApplySaving(){
        savingService.processRecurringSavings();
    }
}
