package com.example.hensley_weighttracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hensley_weighttracker.util.PasswordHasher;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "WeightTracker.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_WEIGHTS = "weights";
    public static final String TABLE_SETTINGS = "settings";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createWeightsTable(db);
        createSettingsTable(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "password_salt TEXT NOT NULL)");
    }

    private void createWeightsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_WEIGHTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "weight_date TEXT NOT NULL, " +
                "weight_value REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");
    }

    private void createSettingsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL UNIQUE, " +
                "goal_weight REAL, " +
                "phone_number TEXT, " +
                "sms_enabled INTEGER NOT NULL DEFAULT 0, " +
                "weight_unit TEXT NOT NULL DEFAULT 'LB', " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateVersionOneToTwo(db);
        }
    }

    private void migrateVersionOneToTwo(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " RENAME TO users_legacy");
            db.execSQL("ALTER TABLE " + TABLE_WEIGHTS + " RENAME TO weights_legacy");
            db.execSQL("ALTER TABLE " + TABLE_SETTINGS + " RENAME TO settings_legacy");

            createUsersTable(db);
            createWeightsTable(db);
            createSettingsTable(db);

            Cursor userCursor = db.rawQuery("SELECT id, username, password FROM users_legacy", null);
            try {
                while (userCursor.moveToNext()) {
                    int id = userCursor.getInt(0);
                    String username = userCursor.getString(1);
                    String legacyPassword = userCursor.getString(2);
                    PasswordHasher.HashedPassword hashedPassword =
                            PasswordHasher.hashPassword(legacyPassword == null ? "" : legacyPassword);

                    ContentValues values = new ContentValues();
                    values.put("id", id);
                    values.put("username", username);
                    values.put("password_hash", hashedPassword.getHash());
                    values.put("password_salt", hashedPassword.getSalt());
                    db.insertOrThrow(TABLE_USERS, null, values);
                }
            } finally {
                userCursor.close();
            }

            db.execSQL("INSERT INTO " + TABLE_WEIGHTS +
                    " (id, user_id, weight_date, weight_value) " +
                    "SELECT id, user_id, weight_date, weight_value FROM weights_legacy");

            db.execSQL("INSERT INTO " + TABLE_SETTINGS +
                    " (id, user_id, goal_weight, phone_number, sms_enabled, weight_unit) " +
                    "SELECT id, user_id, goal_weight, phone_number, sms_enabled, 'LB' FROM settings_legacy");

            db.execSQL("DROP TABLE users_legacy");
            db.execSQL("DROP TABLE weights_legacy");
            db.execSQL("DROP TABLE settings_legacy");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
