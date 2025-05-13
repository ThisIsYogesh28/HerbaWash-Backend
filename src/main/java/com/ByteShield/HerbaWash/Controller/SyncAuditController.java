package com.ByteShield.HerbaWash.Controller;

import com.ByteShield.HerbaWash.Entity.SyncAudit;
import com.ByteShield.HerbaWash.Repository.SyncAuditRepositry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class SyncAuditController {
    private final SyncAuditRepositry auditRepositry;

    public SyncAuditController(SyncAuditRepositry auditRepositry) {
        this.auditRepositry = auditRepositry;
    }
    @GetMapping
    public List<SyncAudit> getAllAudit(){
        return auditRepositry.findAll();
    }

    @GetMapping("/latest")
    public SyncAudit getLatestLog(){
        return auditRepositry.findTopByOrderByTimestampDesc();
    }
}
