package com.example.ExpenseTracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        // Load .env file with better error handling
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Set database properties
            setSystemPropertyIfExists(dotenv, "DB_URL");
            setSystemPropertyIfExists(dotenv, "DB_USER");
            setSystemPropertyIfExists(dotenv, "DB_PASSWORD");

            // Set JWT secret key
            setSystemPropertyIfExists(dotenv, "JWT_SECRET_KEY");

            // Set email properties
            setSystemPropertyIfExists(dotenv, "MAIL_USERNAME");
            setSystemPropertyIfExists(dotenv, "MAIL_PASSWORD");
            setSystemPropertyIfExists(dotenv, "MAIL_FROM");

            // Set server port
            setSystemPropertyIfExists(dotenv, "SERVER_PORT");

            // Set active profile (dev or prod)
            setSystemPropertyIfExists(dotenv, "SPRING_PROFILES_ACTIVE");

        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file - " + e.getMessage());
            System.err.println("Proceeding with system environment variables...");
        }

        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    /**
     * Helper method to set system property if environment variable exists
     */
    private static void setSystemPropertyIfExists(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.trim().isEmpty()) {
            System.setProperty(key, value);
            System.out.println("Loaded: " + key + " = " +
                    (key.contains("PASSWORD") || key.contains("SECRET") ? "****" : value));
        }
    }
}