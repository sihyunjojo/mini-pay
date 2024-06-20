package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.model.Calculate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculateRepository extends JpaRepository<Calculate, Long> {
}
