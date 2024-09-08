package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@Entity
@Table(name = "personal_calculates")
public class PersonalCalculate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance;

    // 정산이 완료 되었는지 확인하는 boolen
    @Column(nullable = false)
    private boolean isCalculated = false;

    @JsonIgnore
    @OneToMany(mappedBy = "personalCalculate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountToPersonalCalculate> accountToPersonalCalculate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id", nullable = false)
    private Calculate calculate;
}
