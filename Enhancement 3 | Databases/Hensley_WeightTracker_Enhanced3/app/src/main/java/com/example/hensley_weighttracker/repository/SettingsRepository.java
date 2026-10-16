package com.example.hensley_weighttracker.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import com.example.hensley_weighttracker.data.AppDatabase;
import com.example.hensley_weighttracker.data.entity.GoalEntity;
import com.example.hensley_weighttracker.data.entity.SettingsEntity;
import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.util.WeightUnit;

public class SettingsRepository {
    private final AppDatabase db;

    public SettingsRepository(Context context) { db = AppDatabase.getInstance(context); }

    public UserSettings getSettings(int userId) {
        // Read goal and preferences from the same snapshot.
        return db.runInTransaction(() -> {
            SettingsEntity settings = db.settings().find(userId);
            GoalEntity goal = db.goals().find(userId);
            return new UserSettings(goal == null ? null : goal.pounds,
                    settings == null || settings.phoneNumber == null ? "" : settings.phoneNumber,
                    settings != null && settings.smsEnabled,
                    settings == null ? WeightUnit.LB : WeightUnit.fromDatabaseValue(settings.weightUnit));
        });
    }

    public boolean saveGoalWeight(int userId, double pounds) {
        try { db.goals().save(userId, pounds); return true; }
        catch (SQLiteConstraintException exception) { return false; }
    }

    public boolean saveNotificationSettings(int userId, String phone, boolean enabled) {
        try { return db.settings().saveNotification(userId, phone, enabled); }
        catch (SQLiteConstraintException exception) { return false; }
    }

    public boolean saveWeightUnit(int userId, WeightUnit unit) {
        try { return db.settings().saveUnit(userId, unit.name()); }
        catch (SQLiteConstraintException exception) { return false; }
    }
}
