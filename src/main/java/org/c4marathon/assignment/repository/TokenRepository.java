package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.model.Token;
import org.c4marathon.assignment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteAllByUserAndTokenType(User user, Token.TokenType tokenType);
}
