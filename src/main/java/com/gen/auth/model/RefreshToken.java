package com.gen.auth.model;


import com.gen.auth.helperutils.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "refresh_token",indexes = @Index(name = "jti_index",columnList = "jti"))
public class RefreshToken extends Model {
    private String tokenReplacedBy;
    @Column(unique = true)
    private String jti;
    @Column(unique = true,nullable = false)
    private Long userId;
    private Instant expiryDate;
    @Column(unique = true,nullable = false)
    private boolean isRevoked;
}
