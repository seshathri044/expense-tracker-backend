package com.example.ExpenseTracker.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb1_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_account_verified")
    private Boolean isAccountVerified;

    @Column(name = "verify_otp")
    private String verifyOtp;

    @Column(name = "verify_otp_expire_at")
    private Long verifyOtpExpireAt;

    @Column(name = "reset_otp")
    private String resetOtp;

    @Column(name = "reset_otp_expires_at")
    private Long resetOtpExpiresAt;  // IMPORTANT: Must be Long type

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}