package com.ByteShield.HerbaWash.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "users")
public class UserEntity {
    @Id
    private String userId;
    private String username;
    private String email;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_real_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> realmRoles;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_client_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> clientRoles;
    private LocalDateTime lastSyncAt;
    private int sessionCount;

}
