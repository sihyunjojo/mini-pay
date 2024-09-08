//package org.c4marathon.assignment.config;
//
//import lombok.RequiredArgsConstructor;
//import org.c4marathon.assignment.model.Account;
//import org.c4marathon.assignment.repository.AccountRepository;
//import org.springframework.batch.core.*;
//import org.springframework.batch.core.configuration.annotation.*;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.item.*;
//import org.springframework.batch.item.database.JpaPagingItemReader;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//public class BankBatchConfig {
//    private JobBuilderFactory jobBuilderFactory;
//    private StepBuilderFactory stepBuilderFactory;
//    private AccountRepository accountRepository; // Assuming you have an Account repository
//
//    @Bean
//    public Job depositAndInterestJob() {
//        return jobBuilderFactory.get("depositAndInterestJob")
//                .incrementer(new RunIdIncrementer())
//                .start(depositStep())
//                .next(interestStep())
//                .build();
//    }
//
//    // 사실 이 경우에는 Step을 2가지로 가르는 것보다 다른 객체와의 상호작업이 없으므로
//    // 이자처리와 함께 해야되는 줄 알았지만,
//    // 입금 처리에서 메인 계좌에 돈이 없으면 예외처리를 해서 입금 처리는 생략하고 이자만 줄 수 있지 않아??
//    // 그럼 두개를 나눠야하는 이유가 있나..?
//    // 없는 것 같다. 그럼 왜?
//    @Bean
//    public Step depositStep() {
//        return stepBuilderFactory.get("depositStep")
//                .<Account, Account>chunk(10)
//                .taskExecutor()
//                .reader(accountReader())
//                .processor(depositProcessor())
//                .writer(accountWriter())
//                .build();
//    }
//
//    @Bean
//    public Step interestStep() {
//        return stepBuilderFactory.get("interestStep")
//                .<Account, Account>chunk(10)
//                .reader(accountReader())
//                .processor(interestProcessor())
//                .writer(accountWriter())
//                .build();
//    }
//
//    @Bean
//    public ItemReader<Account> accountItemReader() {
//        // Example: If using JPA
//        JpaPagingItemReader<Account> reader = new JpaPagingItemReader<>();
//        reader.setEntityManagerFactory(entityManagerFactory);
//        reader.setQueryString("SELECT a FROM Account a");
//        return reader;
//    }
//
//    @Bean
//    public ItemProcessor<Account, Account> depositProcessor() {
//        return account -> {
//            account.setBalance(account.getBalance().add(BigDecimal.valueOf(100)));
//            return account;
//        };
//    }
//
//    @Bean
//    public ItemProcessor<Account, Account> interestProcessor() {
//        return account -> {
//            BigDecimal balance = account.getBalance();
//            BigDecimal interestAmount = balance.multiply(BigDecimal.valueOf(0.001)); // 0.1% of the balance
//            account.setBalance(balance.add(interestAmount));
//            return account;
//        };
//    }
//
//    @Bean
//    public ItemWriter<Account> accountItemWriter() {
//        return accounts -> {
//            for (Account account : accounts) {
//                accountRepository.save(account); // Update the account balance
//            }
//        };
//    }
//}
