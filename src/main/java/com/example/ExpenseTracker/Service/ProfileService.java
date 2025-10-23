package com.example.ExpenseTracker.Service;

import com.example.ExpenseTracker.IO.ProfileRequest;
import com.example.ExpenseTracker.IO.ProfileResponse;
import com.example.ExpenseTracker.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);

    void resetPassword(String email , String otp, String newPassword);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    String getLoggedInUserId(String email);
}
