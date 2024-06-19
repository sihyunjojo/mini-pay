package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.c4marathon.assignment.util.AccountType;

import java.math.BigDecimal;

@Table(name = "accounts")
@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String second_type = AccountType.RECURRING_SAVINGS;

    // 어떻게 저장이 될까?? ->
    @Column(nullable = false)
    private BigDecimal balance;

    @Column
    private BigDecimal savingBalance;

    @Column(nullable = false)
    private BigDecimal totalChargedInPeriod = BigDecimal.ZERO; // track daily limit

    @Version
    private Long version;

    //java.lang.IllegalStateException: Cannot call sendError() after the response has been committed 해결
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalChargedInPeriod = this.totalChargedInPeriod.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public boolean isRecurringSavings() {
        return AccountType.RECURRING_SAVINGS.equals(this.second_type);
    }

    public boolean isFlexibleSavings() {
        return AccountType.FLEXIBLE_SAVINGS.equals(this.second_type);
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