package com.example.hensley_weighttracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.hensley_weighttracker.data.DBHelper;
import com.example.hensley_weighttracker.model.UserCredentials;

public class UserRepository {
    private final DBHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new DBHelper(context.getApplicationContext());
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + DBHelper.TABLE_USERS + " WHERE username = ? LIMIT 1",
                new String[]{username}
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public int createUser(String username, String passwordHash, String passwordSalt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", passwordHash);
        values.put("password_salt", passwordSalt);

        long result = db.insert(DBHelper.TABLE_USERS, null, values);
        return result == -1 ? -1 : (int) result;
    }

    public UserCredentials findCredentialsByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, password_hash, password_salt FROM " + DBHelper.TABLE_USERS +
                        " WHERE username = ?",
                new String[]{username}
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new UserCredentials(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
        } finally {
            cursor.close();
        }
    }
}
