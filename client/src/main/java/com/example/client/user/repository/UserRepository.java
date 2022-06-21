package com.example.client.user.repository;

import com.example.client.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteUserById(Long id);

    Optional<User> findByEmail(String email);
    Optional<User>findByUserName(String username);
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);

    User getUserByUserName(String username);



}
