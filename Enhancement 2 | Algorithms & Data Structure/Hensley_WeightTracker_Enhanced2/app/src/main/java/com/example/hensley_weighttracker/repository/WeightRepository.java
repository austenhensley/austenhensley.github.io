package com.example.hensley_weighttracker.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.hensley_weighttracker.data.DBHelper;
import com.example.hensley_weighttracker.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public class WeightRepository {
    private final DBHelper dbHelper;

    public WeightRepository(Context context) {
        dbHelper = new DBHelper(context.getApplicationContext());
    }

    public boolean addWeight(int userId, String date, double pounds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("weight_date", date);
        values.put("weight_value", pounds);
        return db.insert(DBHelper.TABLE_WEIGHTS, null, values) != -1;
    }

    public boolean updateWeight(int weightId, int userId, String date, double pounds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("weight_date", date);
        values.put("weight_value", pounds);

        int rows = db.update(
                DBHelper.TABLE_WEIGHTS,
                values,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );
        return rows > 0;
    }

    public boolean deleteWeight(int weightId, int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DBHelper.TABLE_WEIGHTS,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );
        return rows > 0;
    }

    public WeightEntry getWeightById(int weightId, int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, weight_date, weight_value FROM " + DBHelper.TABLE_WEIGHTS +
                        " WHERE id = ? AND user_id = ?",
                new String[]{String.valueOf(weightId), String.valueOf(userId)}
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new WeightEntry(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2));
        } finally {
            cursor.close();
        }
    }

    public List<WeightEntry> getAllWeights(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, weight_date, weight_value FROM " + DBHelper.TABLE_WEIGHTS +
                        " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );

        List<WeightEntry> entries = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                entries.add(new WeightEntry(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)));
            }
        } finally {
            cursor.close();
        }
        return entries;
    }
}
