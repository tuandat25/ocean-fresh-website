package com.tuandat.oceanfresh_backend.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.Token;
import com.tuandat.oceanfresh_backend.models.User;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUser(User user);
    Token findByToken(String token);
    Token findByRefreshToken(String token);
}

