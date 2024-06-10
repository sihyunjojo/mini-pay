package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.type = 'MAIN'")
    Optional<Account> findMainAccountByUserId(@Param("userId") Long userId);
}