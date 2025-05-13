package com.ByteShield.HerbaWash.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "revocation_logs")
@Data
public class TokenRevocationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String userId;
    private String tokenHash;
    private LocalDateTime revokedAt;
}
