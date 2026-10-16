package com.example.hensley_weighttracker.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.hensley_weighttracker.data.dao.*;
import com.example.hensley_weighttracker.data.entity.*;

@Database(entities = {UserEntity.class, WeightEntity.class, SettingsEntity.class, GoalEntity.class},
        version = 3, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract UserDao users();
    public abstract WeightDao weights();
    public abstract SettingsDao settings();
    public abstract GoalDao goals();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "WeightTracker.db")
                            .addMigrations(DatabaseMigrations.FROM_1_TO_3, DatabaseMigrations.FROM_2_TO_3)
                            .addCallback(new Callback() {
                                @Override public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    DatabaseRules.install(db);
                                }
                            }).build();
                }
            }
        }
        return instance;
    }
}
