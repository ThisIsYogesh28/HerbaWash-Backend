package com.ByteShield.HerbaWash.Repository;

import com.ByteShield.HerbaWash.Entity.SyncAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncAuditRepositry extends JpaRepository<SyncAudit,Long> {
    SyncAudit findTopByOrderByTimestampDesc();
}
