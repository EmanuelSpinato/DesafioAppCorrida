package com.example.rideservice.repository;

import com.example.rideservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // buscar um usuário pelo email
}
