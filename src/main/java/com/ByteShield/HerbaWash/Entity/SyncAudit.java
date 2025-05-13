package com.ByteShield.HerbaWash.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SyncAudit {
    @Id

    private UUID id;
    private String status;
    private LocalDateTime timestamp;
    private String triggerBy;
    private int userSynced;

    @Lob
    private String errorDetails;
}
