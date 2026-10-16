package com.example.hensley_weighttracker.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import com.example.hensley_weighttracker.data.AppDatabase;
import com.example.hensley_weighttracker.data.dao.UserDao;
import com.example.hensley_weighttracker.data.entity.UserEntity;
import com.example.hensley_weighttracker.model.UserCredentials;

public class UserRepository {
    private final UserDao users;

    public UserRepository(Context context) {
        users = AppDatabase.getInstance(context).users();
    }

    public boolean usernameExists(String username) { return users.usernameExists(username); }

    public int createUser(String username, String hash, String salt) {
        UserEntity user = new UserEntity();
        user.username = username;
        user.passwordHash = hash;
        user.passwordSalt = salt;
        try { return Math.toIntExact(users.insert(user)); }
        catch (SQLiteConstraintException exception) { return -1; }
    }

    public UserCredentials findCredentialsByUsername(String username) {
        UserEntity user = users.findByUsername(username);
        return user == null ? null : new UserCredentials(user.id, user.passwordHash, user.passwordSalt);
    }
}
