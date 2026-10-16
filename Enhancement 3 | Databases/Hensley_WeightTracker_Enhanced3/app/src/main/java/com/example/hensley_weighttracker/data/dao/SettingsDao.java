package com.example.hensley_weighttracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import com.example.hensley_weighttracker.data.entity.SettingsEntity;

@Dao
public abstract class SettingsDao {
    @Query("SELECT * FROM settings WHERE user_id = :userId")
    public abstract SettingsEntity find(int userId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(SettingsEntity settings);

    @Query("UPDATE settings SET phone_number = :phone, sms_enabled = :enabled WHERE user_id = :userId")
    public abstract int updateNotification(int userId, String phone, boolean enabled);

    @Query("UPDATE settings SET weight_unit = :unit WHERE user_id = :userId")
    public abstract int updateUnit(int userId, String unit);

    private void ensureRow(int userId) {
        SettingsEntity settings = new SettingsEntity();
        settings.userId = userId;
        insert(settings);
    }

    @Transaction
    public boolean saveNotification(int userId, String phone, boolean enabled) {
        ensureRow(userId);
        return updateNotification(userId, phone, enabled) == 1;
    }

    @Transaction
    public boolean saveUnit(int userId, String unit) {
        ensureRow(userId);
        return updateUnit(userId, unit) == 1;
    }
}
