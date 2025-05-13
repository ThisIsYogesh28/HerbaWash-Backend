package com.ByteShield.HerbaWash.Repository;

import com.ByteShield.HerbaWash.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByUsername(String username);
}
