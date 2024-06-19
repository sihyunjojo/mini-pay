package org.c4marathon.assignment.config;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.service.AccountService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private AccountService accountService;

    // JobRepository는 Job과 Step의 실행 정보를 저장하고 관리하는 역할을 합니다.
    // transactionManager: 트랜잭션 관리자를 지정하여 이 Step에서 실행될 작업이 트랜잭션 내에서 안전하게 실행되도록 합니다. 이로 인해 작업이 부분적으로 실행되지 않고, 전체적으로 성공하거나 실패하게 됩니다.

    @Bean
    // Job: 하나 이상의 Step을 포함하는 배치 작업의 전체 정의입니다.
    // 여러 Step을 순차적으로 실행하거나 병렬로 실행할 수 있습니다.
    public Job resetDailyLimitJob(JobRepository jobRepository, @Qualifier("resetDailyLimitStep") Step resetDailyLimitStep) {
        // JobBuilder를 사용하여 배치 작업을 빌드하고 반환합니다.
        // 이 Job의 이름을 지정합니다. Job 이름은 고유해야 하며, 나중에 이 Job을 식별할 때 사용됩니다.
        return new JobBuilder("resetDailyLimitJob", jobRepository)
                .start(resetDailyLimitStep) // Job의 첫 번째 단계를 resetDailyLimitStep으로 지정합니다.
                .build();
    }

    @Bean
    // Step: Job 내에서 실제로 수행되는 작업의 단위입니다.
    // 특정 작업을 수행하며, 트랜잭션 관리, 오류 처리 등을 관리합니다.
    public Step resetDailyLimitStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // StepBuilder를 사용하여 단계에 태스크릿을 설정하고 반환합니다.
        // 이 Step의 이름을 지정합니다. Step 이름은 고유해야 하며, 나중에 이 Step을 식별할 때 사용됩니다.
        return new StepBuilder("resetDailyLimitStep", jobRepository) // 이 단계의 고유한 이름을 정함.
                .tasklet(resetDailyLimitTasklet(), transactionManager) // StepBuilder에 Tasklet을 추가하고, 이 Tasklet이 수행될 때 사용할 트랜잭션 관리자를 설정합니다.
                .build();
    }

    // 이 작업은 트랜잭션 관리, 작업 재시작 및 실패 관리와 같은 배치 처리 기능을 포함합니다.
    @Bean
    // Tasklet : Step 내에서 수행되는 실제 작업 로직을 구현합니다.
    // 단일 태스크로 작업을 처리합니다.
    public Tasklet resetDailyLimitTasklet() {
        // 태스크릿은 accountService.resetDailyLimits()를 호출하여 모든 계정의 일일 한도를 재설정합니다.
        // contribution: StepContribution 객체로, 현재 Step의 실행 상태를 나타내고 업데이트합니다.
        // chunkContext: ChunkContext 객체로, 현재 Chunk의 실행 컨텍스트를 제공합니다.
        return (contribution, chunkContext) -> {
            // Logic to reset daily limit for all accounts
            accountService.resetDailyLimits();
            return RepeatStatus.FINISHED;
        };
    }
}