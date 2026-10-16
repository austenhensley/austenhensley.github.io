package com.example.hensley_weighttracker.model;

public class UserCredentials {
    private final int userId;
    private final String passwordHash;
    private final String passwordSalt;

    public UserCredentials(int userId, String passwordHash, String passwordSalt) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public int getUserId() {
        return userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
