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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account fromAccount;

    @JsonIgnore
    @OneToMany(mappedBy = "personal_calculate_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonalCalculate> personalCalculate;

    private BigDecimal totalAmount;
    private String type; // "1/n" or "random"


}
