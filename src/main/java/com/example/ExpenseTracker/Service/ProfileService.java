package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ProfileResponse;

public interface ProfileService {

    // NEW: Registration flow methods (in-memory storage)
    void initiateRegistration(ProfileRequest request);
    void sendRegistrationOtp(String email);
    void verifyRegistrationOtp(String email, String otp);

    // Existing methods
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);

    // Password reset methods
    void sendResetOtp(String email);
    void resetPassword(String email, String otp, String newPassword);

    // DEPRECATED: Old OTP methods (for existing verified users)
    @Deprecated
    void sendOtp(String email);
    @Deprecated
    void verifyOtp(String email, String otp);

    String getLoggedInUserId(String email);
}