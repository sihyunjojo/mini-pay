package org.c4marathon.assignment.util;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DailyJobScheduler {

    private final JobLauncher jobLauncher; // 일괄 작업을 시작하는 데 사용됩니다.
    private final Job resetDailyLimitJob; // BatchConfig 클래스에 정의된 작업입니다.


    // 매일 자정에 dailyResetJob 배치 작업이 실행되어 dailyResetTasklet에 정의된 작업을 수행하게 됩니다.
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 메서드가 실행되도록 예약합니다.
    public void runResetDailyLimitJob() {
        try {
            jobLauncher.run(resetDailyLimitJob, new JobParametersBuilder()
                    .addString("runTime", LocalDateTime.now().toString())
                    .toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}