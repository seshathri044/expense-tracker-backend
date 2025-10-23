package com.example.ExpenseTracker.IO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "Name should be not Empty")
    private String name;
    @Email(message = "Enter valid Email Address")
    @NotNull(message = "Email should not be Empty")
    private String email;
    @Size(min = 6, message = "Password must be atleast 6 chracters")
    private String password;


}
