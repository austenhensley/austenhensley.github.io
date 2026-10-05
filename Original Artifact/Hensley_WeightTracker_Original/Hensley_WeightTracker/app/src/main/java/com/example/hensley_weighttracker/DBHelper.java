package com.example.hensley_weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_WEIGHTS = "weights";
    public static final String TABLE_SETTINGS = "settings";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT)";

        String createWeightsTable = "CREATE TABLE " + TABLE_WEIGHTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "weight_date TEXT, " +
                "weight_value REAL)";

        String createSettingsTable = "CREATE TABLE " + TABLE_SETTINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "goal_weight REAL, " +
                "phone_number TEXT, " +
                "sms_enabled INTEGER DEFAULT 0)";

        db.execSQL(createUsersTable);
        db.execSQL(createWeightsTable);
        db.execSQL(createSettingsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ?",
                new String[]{username}
        );

        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ? AND password = ?",
                new String[]{username, password}
        );

        int userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        return userId;
    }

    public boolean addWeight(int userId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("weight_date", date);
        values.put("weight_value", weight);

        long result = db.insert(TABLE_WEIGHTS, null, values);
        return result != -1;
    }

    public boolean updateWeight(int weightId, int userId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("weight_date", date);
        values.put("weight_value", weight);

        int rows = db.update(
                TABLE_WEIGHTS,
                values,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );

        return rows > 0;
    }

    public boolean deleteWeight(int weightId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rows = db.delete(
                TABLE_WEIGHTS,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );

        return rows > 0;
    }

    public Cursor getAllWeights(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, weight_date, weight_value FROM " + TABLE_WEIGHTS +
                        " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getWeightById(int weightId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, weight_date, weight_value FROM " + TABLE_WEIGHTS +
                        " WHERE id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );
    }

    public String getWeightDate(int weightId, int userId) {
        String date = "";

        Cursor cursor = getWeightById(weightId, userId);

        if (cursor.moveToFirst()) {
            date = cursor.getString(1);
        }

        cursor.close();
        return date;
    }

    public Double getGoalWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT goal_weight FROM " + TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        Double goalWeight = null;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            goalWeight = cursor.getDouble(0);
        }

        cursor.close();
        return goalWeight;
    }

    public void saveGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("goal_weight", goalWeight);

        if (cursor.moveToFirst()) {
            db.update(
                    TABLE_SETTINGS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
        } else {
            db.insert(TABLE_SETTINGS, null, values);
        }

        cursor.close();
    }

    public void saveNotificationSettings(int userId, String phoneNumber, int smsEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("phone_number", phoneNumber);
        values.put("sms_enabled", smsEnabled);

        if (cursor.moveToFirst()) {
            db.update(
                    TABLE_SETTINGS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
        } else {
            db.insert(TABLE_SETTINGS, null, values);
        }

        cursor.close();
    }

    public String getPhoneNumber(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT phone_number FROM " + TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        String phoneNumber = "";

        if (cursor.moveToFirst() && cursor.getString(0) != null) {
            phoneNumber = cursor.getString(0);
        }

        cursor.close();
        return phoneNumber;
    }

    public int isSmsEnabled(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT sms_enabled FROM " + TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        int enabled = 0;

        if (cursor.moveToFirst()) {
            enabled = cursor.getInt(0);
        }

        cursor.close();
        return enabled;
    }
}