package com.example.hensley_weighttracker.util;

public final class InputValidator {
    private static final double MIN_WEIGHT_LB = 1.0;
    private static final double MAX_WEIGHT_LB = 1500.0;

    private InputValidator() {
    }

    public static ValidationResult validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return ValidationResult.invalid("Enter a username and password.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateNewAccount(String username, String password) {
        ValidationResult basicResult = validateLogin(username, password);
        if (!basicResult.isValid()) {
            return basicResult;
        }

        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 30) {
            return ValidationResult.invalid("Username must be 3 to 30 characters.");
        }

        if (!trimmedUsername.matches("[A-Za-z0-9._-]+")) {
            return ValidationResult.invalid("Username can only contain letters, numbers, periods, underscores, and hyphens.");
        }

        if (password.length() < 8) {
            return ValidationResult.invalid("Password must be at least 8 characters.");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateWeightText(String weightText) {
        if (weightText == null || weightText.trim().isEmpty()) {
            return ValidationResult.invalid("Enter a weight value.");
        }

        try {
            double weight = Double.parseDouble(weightText.trim());
            if (!Double.isFinite(weight) || weight <= 0) {
                return ValidationResult.invalid("Enter a valid positive weight.");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.invalid("Enter a valid numeric weight.");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateStoredWeightPounds(double pounds) {
        if (!Double.isFinite(pounds) || pounds < MIN_WEIGHT_LB || pounds > MAX_WEIGHT_LB) {
            return ValidationResult.invalid("Enter a weight between 1 and 1500 pounds (0.5 to 680 kilograms).");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ValidationResult.invalid("Enter a phone number first.");
        }

        String trimmed = phoneNumber.trim();
        if (!trimmed.matches("\\+?[0-9() .-]{7,20}")) {
            return ValidationResult.invalid("Enter a valid phone number.");
        }

        return ValidationResult.valid();
    }
}
