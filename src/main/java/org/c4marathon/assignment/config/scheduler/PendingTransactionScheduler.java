package org.c4marathon.assignment.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.service.PendingTransactionService;
import org.c4marathon.assignment.service.TransferService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 배치 작업과 달리 트랜잭션 관리나 작업 재시작 기능이 필요하지 않습니다.
public class PendingTransactionScheduler {

    private final PendingTransactionService pendingTransactionService;

    // 24시간 남은 트랜잭션에 알림 전송 (매시간 실행)
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void sendReminders() {
        pendingTransactionService.sendReminders();
    }

    // 만료된 트랜잭션 처리 (매시간 실행)
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void expirePendingTransactions() {
        pendingTransactionService.expirePendingTransactions();
    }
}
