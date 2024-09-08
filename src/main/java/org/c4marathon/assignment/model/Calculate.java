package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Table(name = "calculates")
@Entity
@Data
public class Calculate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String type;

    // 본인은 냈음.
    @Column(nullable = false)
    private int numberOfIsCalculated;

    @JsonIgnore
    @OneToMany(mappedBy = "calculate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonalCalculate> personalCalculate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account fromAccount;
}
