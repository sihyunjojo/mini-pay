package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static org.c4marathon.assignment.util.AccountType.*;

@Table(name = "accounts")
@Entity
@Data
public class Account {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "example_seq")
    @SequenceGenerator(name = "example_seq", sequenceName = "example_sequence", initialValue = 100, allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String type;

    private String second_type = MAIN;

    // mysql에서 BigDecimal은 DECIMAL로 매핑된다.
    // DECIMAL은 정확한 숫자를 저장할 수 있는 데이터 타입이다.
    //
    @Column(nullable = false, precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal totalChargedInPeriod = BigDecimal.ZERO; // track daily limit

    // 저축하는 금액
    @Column
    private BigDecimal savingBalance;


    @JsonIgnore
    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calculate> calculate;

    //java.lang.IllegalStateException: Cannot call sendError() after the response has been committed 해결
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_to_personal_calculate_id")
    private AccountToPersonalCalculate accountToPersonalCalculate;

    @Version
    private Long version;


    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalChargedInPeriod = this.totalChargedInPeriod.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public boolean isRecurringSavings() {
        return RECURRING_SAVINGS.equals(this.second_type);
    }

    public boolean isFlexibleSavings() {
        return FLEXIBLE_SAVINGS.equals(this.second_type);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", balance=" + balance +
                ", userName=" + user.getName() +
                ", userID=" + user.getId() +
                '}';
    }
}