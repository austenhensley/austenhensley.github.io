package com.example.hensley_weighttracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.hensley_weighttracker.data.DBHelper;
import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.util.WeightUnit;

public class SettingsRepository {
    private final DBHelper dbHelper;

    public SettingsRepository(Context context) {
        dbHelper = new DBHelper(context.getApplicationContext());
    }

    public UserSettings getSettings(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT goal_weight, phone_number, sms_enabled, weight_unit FROM " +
                        DBHelper.TABLE_SETTINGS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        try {
            if (!cursor.moveToFirst()) {
                return new UserSettings(null, "", false, WeightUnit.LB);
            }

            Double goal = cursor.isNull(0) ? null : cursor.getDouble(0);
            String phone = cursor.isNull(1) ? "" : cursor.getString(1);
            boolean smsEnabled = cursor.getInt(2) == 1;
            WeightUnit unit = WeightUnit.fromDatabaseValue(cursor.getString(3));
            return new UserSettings(goal, phone, smsEnabled, unit);
        } finally {
            cursor.close();
        }
    }

    public boolean saveGoalWeight(int userId, double goalWeightPounds) {
        ensureSettingsRow(userId);
        ContentValues values = new ContentValues();
        values.put("goal_weight", goalWeightPounds);
        return updateSettings(userId, values);
    }

    public boolean saveNotificationSettings(int userId, String phoneNumber, boolean enabled) {
        ensureSettingsRow(userId);
        ContentValues values = new ContentValues();
        values.put("phone_number", phoneNumber);
        values.put("sms_enabled", enabled ? 1 : 0);
        return updateSettings(userId, values);
    }

    public boolean saveWeightUnit(int userId, WeightUnit unit) {
        ensureSettingsRow(userId);
        ContentValues values = new ContentValues();
        values.put("weight_unit", unit.name());
        return updateSettings(userId, values);
    }

    private void ensureSettingsRow(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        db.insertWithOnConflict(
                DBHelper.TABLE_SETTINGS,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    private boolean updateSettings(int userId, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(
                DBHelper.TABLE_SETTINGS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)}
        ) > 0;
    }
}
