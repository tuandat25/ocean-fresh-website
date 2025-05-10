package com.tuandat.oceanfresh_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tuandat.oceanfresh_backend.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
}
