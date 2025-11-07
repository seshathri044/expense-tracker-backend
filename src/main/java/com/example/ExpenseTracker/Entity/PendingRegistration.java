package com.example.ExpenseTracker.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Temporary model for storing unverified user registrations in memory
 * NOT saved to database until OTP is verified
 *
 * NOTE: This is NOT a JPA entity - just a POJO for in-memory storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRegistration {

    private String name;
    private String email;
    private String password; // Already encoded
    private String otp;
    private Long otpExpiresAt;
    private Long createdAt;

    /**
     * Check if pending registration has expired (1 hour timeout)
     */
    public boolean isExpired() {
        if (createdAt == null) {
            return true;
        }
        long oneHourInMillis = 60 * 60 * 1000;
        return System.currentTimeMillis() - createdAt > oneHourInMillis;
    }

    /**
     * Check if OTP has expired (10 minutes timeout)
     */
    public boolean isOtpExpired() {
        if (otpExpiresAt == null) {
            return true;
        }
        return System.currentTimeMillis() > otpExpiresAt;
    }
}
