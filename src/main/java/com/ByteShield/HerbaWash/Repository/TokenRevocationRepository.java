package com.ByteShield.HerbaWash.Repository;

import com.ByteShield.HerbaWash.Entity.TokenRevocationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocationRepository extends JpaRepository<TokenRevocationLog,Long> {
}
