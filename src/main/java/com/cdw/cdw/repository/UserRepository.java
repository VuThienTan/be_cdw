package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmailOrUsername(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByCodeActive(String code);

    Optional<User> findByUsername(String username);
    List<User> findByActiveTrue();
}
