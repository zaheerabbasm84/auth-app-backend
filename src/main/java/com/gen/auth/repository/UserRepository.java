package com.gen.auth.repository;

import com.gen.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailId(String email);
    boolean existsByUserNameAndOrganizationId(String userName, Long organizationId);
    boolean existsByUserNameAndOrganizationIdAndIdNot(String userName,Long organizationId,Long id);
}
