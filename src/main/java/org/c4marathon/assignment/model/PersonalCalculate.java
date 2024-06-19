package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@Entity
@Table(name = "personalCalculates")
public class PersonalCalculate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal balance;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id", nullable = false)
    private Calculate calculate;

    @JsonIgnore
    @OneToMany(mappedBy = "user_to_personal_calculate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserToPersonalCalculate> userToPersonalCalculate;

    private BigDecimal amount;

}
