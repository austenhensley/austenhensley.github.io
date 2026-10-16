package com.example.hensley_weighttracker.service;

import android.content.Context;

import com.example.hensley_weighttracker.model.UserCredentials;
import com.example.hensley_weighttracker.repository.UserRepository;
import com.example.hensley_weighttracker.util.InputValidator;
import com.example.hensley_weighttracker.util.PasswordHasher;
import com.example.hensley_weighttracker.util.ValidationResult;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(Context context) {
        userRepository = new UserRepository(context);
    }

    public ServiceResult<Integer> login(String username, String password) {
        ValidationResult validation = InputValidator.validateLogin(username, password);
        if (!validation.isValid()) {
            return ServiceResult.failure(validation.getMessage());
        }

        UserCredentials credentials = userRepository.findCredentialsByUsername(username.trim());
        if (credentials == null || !PasswordHasher.verifyPassword(
                password,
                credentials.getPasswordHash(),
                credentials.getPasswordSalt())) {
            return ServiceResult.failure("Invalid username or password.");
        }

        return ServiceResult.success(credentials.getUserId(), "Login successful.");
    }

    public ServiceResult<Integer> createAccount(String username, String password) {
        ValidationResult validation = InputValidator.validateNewAccount(username, password);
        if (!validation.isValid()) {
            return ServiceResult.failure(validation.getMessage());
        }

        String normalizedUsername = username.trim();
        if (userRepository.usernameExists(normalizedUsername)) {
            return ServiceResult.failure("Username already exists.");
        }

        try {
            PasswordHasher.HashedPassword hashedPassword = PasswordHasher.hashPassword(password);
            int userId = userRepository.createUser(
                    normalizedUsername,
                    hashedPassword.getHash(),
                    hashedPassword.getSalt()
            );

            if (userId == -1) {
                return ServiceResult.failure("Unable to create account.");
            }
            return ServiceResult.success(userId, "Account created. You can now log in.");
        } catch (IllegalStateException e) {
            return ServiceResult.failure("Unable to securely create the account.");
        }
    }
}
