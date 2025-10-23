// AuthResponse.java
package com.example.ExpenseTracker.IO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String email;
    private String token;
    private boolean error = false;
    private String message = "Login successful";

    public AuthResponse(String email, String token) {
        this.email = email;
        this.token = token;
        this.error = false;
        this.message = "Login successful";
    }
}