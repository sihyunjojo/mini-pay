package org.c4marathon.assignment.repository;


import org.c4marathon.assignment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);

    @Query("SELECT u FROM User u JOIN u.accounts a WHERE a.id = :accountId")
    User findByAccountId(@Param("accountId") Long accountId);
}
