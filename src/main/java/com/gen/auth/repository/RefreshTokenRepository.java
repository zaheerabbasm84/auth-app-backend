package com.gen.auth.repository;

import com.gen.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    public Optional<RefreshToken> findByUserId(Long userId);
    public Optional<RefreshToken> findByJti(String jti);
}
