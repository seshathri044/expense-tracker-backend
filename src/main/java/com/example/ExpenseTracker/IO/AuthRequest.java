// AuthRequest.java
package com.example.ExpenseTracker.IO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Email(message = "Enter valid Email Address")
    @NotBlank(message = "Email should not be empty")
    private String email;

    @NotBlank(message = "Password should not be empty")
    private String password;
}