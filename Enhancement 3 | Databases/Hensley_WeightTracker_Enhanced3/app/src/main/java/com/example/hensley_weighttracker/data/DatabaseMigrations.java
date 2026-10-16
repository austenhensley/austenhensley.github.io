package com.example.hensley_weighttracker.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.hensley_weighttracker.util.DateCodec;
import com.example.hensley_weighttracker.util.InputValidator;
import com.example.hensley_weighttracker.util.PasswordHasher;

public final class DatabaseMigrations {
    private DatabaseMigrations() { }

    public static final Migration FROM_1_TO_3 = migrationFrom(1);
    public static final Migration FROM_2_TO_3 = migrationFrom(2);

    private static Migration migrationFrom(int oldVersion) {
        return new Migration(oldVersion, 3) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase db) {
                migrateLegacy(db, oldVersion);
            }
        };
    }

    private static void migrateLegacy(SupportSQLiteDatabase db, int oldVersion) {
        // Room runs this migration inside a transaction. A failed check rolls it all back.
        long userSequence = sequence(db, "users");
        long weightSequence = sequence(db, "weights");
        long settingsSequence = sequence(db, "settings");

        createReplacementTables(db);
        copyUsers(db, oldVersion);
        copyWeights(db);
        copySettingsAndGoals(db, oldVersion);

        checkCount(db, "users", "users_new");
        checkCount(db, "weights", "weights_new");
        checkCount(db, "settings", "settings_new");

        try (Cursor cursor = db.query(
                "SELECT COUNT(*) FROM settings WHERE goal_weight IS NOT NULL")) {
            cursor.moveToFirst();
            if (cursor.getLong(0) != count(db, "goals_new")) {
                throw new IllegalStateException("Saved goal count changed during migration.");
            }
        }

        try (Cursor cursor = db.query("PRAGMA foreign_key_check")) {
            if (cursor.moveToFirst()) {
                throw new IllegalStateException("Invalid saved user relationship.");
            }
        }

        // Remove children before parents so cascading deletes cannot erase copied records.
        db.execSQL("DROP TABLE weights");
        db.execSQL("DROP TABLE settings");
        db.execSQL("DROP TABLE users");
        db.execSQL("ALTER TABLE users_new RENAME TO users");

        // Build final child tables explicitly; Android may retain users_new in renamed foreign keys.
        createFinalChildTables(db);
        db.execSQL("INSERT INTO weights SELECT * FROM weights_new");
        db.execSQL("INSERT INTO settings SELECT * FROM settings_new");
        db.execSQL("INSERT INTO goals SELECT * FROM goals_new");

        db.execSQL("DROP TABLE weights_new");
        db.execSQL("DROP TABLE settings_new");
        db.execSQL("DROP TABLE goals_new");

        try (Cursor cursor = db.query("PRAGMA foreign_key_check")) {
            if (cursor.moveToFirst()) {
                throw new IllegalStateException("Invalid migrated user relationship.");
            }
        }

        db.execSQL("CREATE UNIQUE INDEX index_users_username ON users(username)");
        db.execSQL("CREATE INDEX index_weights_user_id_weight_date ON weights(user_id, weight_date)");
        db.execSQL("CREATE UNIQUE INDEX index_settings_user_id ON settings(user_id)");
        db.execSQL("CREATE UNIQUE INDEX index_goals_user_id ON goals(user_id)");

        restoreSequence(db, "users", userSequence);
        restoreSequence(db, "weights", weightSequence);
        restoreSequence(db, "settings", settingsSequence);

        DatabaseRules.install(db);
    }

    private static void createReplacementTables(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE users_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "username TEXT NOT NULL, password_hash TEXT NOT NULL, password_salt TEXT NOT NULL)");

        db.execSQL("CREATE TABLE weights_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, weight_date TEXT NOT NULL, weight_value REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users_new(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE settings_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, phone_number TEXT, sms_enabled INTEGER NOT NULL DEFAULT 0, " +
                "weight_unit TEXT NOT NULL DEFAULT 'LB', " +
                "FOREIGN KEY(user_id) REFERENCES users_new(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE goals_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, goal_weight REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users_new(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
    }

    private static void createFinalChildTables(SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE weights (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, weight_date TEXT NOT NULL, weight_value REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE settings (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, phone_number TEXT, sms_enabled INTEGER NOT NULL DEFAULT 0, " +
                "weight_unit TEXT NOT NULL DEFAULT 'LB', " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "user_id INTEGER NOT NULL, goal_weight REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
    }

    private static void copyUsers(SupportSQLiteDatabase db, int oldVersion) {
        String sql = oldVersion == 1
                ? "SELECT id, username, password FROM users"
                : "SELECT id, username, password_hash, password_salt FROM users";

        try (Cursor cursor = db.query(sql)) {
            while (cursor.moveToNext()) {
                if (cursor.isNull(1) || cursor.isNull(2)
                        || (oldVersion == 2 && cursor.isNull(3))) {
                    throw new IllegalStateException("Invalid saved credentials.");
                }

                String hash;
                String salt;

                if (oldVersion == 1) {
                    PasswordHasher.HashedPassword password =
                            PasswordHasher.hashPassword(cursor.getString(2));
                    hash = password.getHash();
                    salt = password.getSalt();
                } else {
                    hash = cursor.getString(2);
                    salt = cursor.getString(3);
                }

                db.execSQL(
                        "INSERT INTO users_new(id, username, password_hash, password_salt) VALUES(?,?,?,?)",
                        new Object[]{cursor.getInt(0), cursor.getString(1), hash, salt});
            }
        }
    }

    private static void copyWeights(SupportSQLiteDatabase db) {
        try (Cursor cursor = db.query(
                "SELECT id, user_id, weight_date, weight_value FROM weights")) {
            while (cursor.moveToNext()) {
                if (cursor.isNull(1) || cursor.isNull(3)) {
                    throw new IllegalStateException("Invalid saved weight.");
                }

                double pounds = cursor.getDouble(3);
                if (!InputValidator.validateStoredWeightPounds(pounds).isValid()) {
                    throw new IllegalStateException("Invalid saved weight.");
                }

                String date = DateCodec.toStored(cursor.getString(2));

                db.execSQL(
                        "INSERT INTO weights_new(id,user_id,weight_date,weight_value) VALUES(?,?,?,?)",
                        new Object[]{cursor.getInt(0), cursor.getInt(1), date, pounds});
            }
        }
    }

    private static void copySettingsAndGoals(SupportSQLiteDatabase db, int oldVersion) {
        String sql = "SELECT id,user_id,goal_weight,phone_number,sms_enabled"
                + (oldVersion == 2 ? ",weight_unit" : "") + " FROM settings";

        try (Cursor cursor = db.query(sql)) {
            while (cursor.moveToNext()) {
                if (cursor.isNull(1)) {
                    throw new IllegalStateException("Invalid saved settings user.");
                }

                int userId = cursor.getInt(1);
                int sms = cursor.isNull(4) ? 0 : cursor.getInt(4);
                String unit = oldVersion == 1 ? "LB" : cursor.getString(5);

                if ((sms != 0 && sms != 1)
                        || !("LB".equals(unit) || "KG".equals(unit))) {
                    throw new IllegalStateException("Invalid saved preferences.");
                }

                db.execSQL(
                        "INSERT INTO settings_new(id,user_id,phone_number,sms_enabled,weight_unit) VALUES(?,?,?,?,?)",
                        new Object[]{cursor.getInt(0), userId, cursor.getString(3), sms, unit});

                if (!cursor.isNull(2)) {
                    double goal = cursor.getDouble(2);
                    if (!InputValidator.validateStoredWeightPounds(goal).isValid()) {
                        throw new IllegalStateException("Invalid saved goal.");
                    }

                    db.execSQL(
                            "INSERT INTO goals_new(user_id,goal_weight) VALUES(?,?)",
                            new Object[]{userId, goal});
                }
            }
        }
    }

    private static void checkCount(SupportSQLiteDatabase db, String oldTable, String newTable) {
        if (count(db, oldTable) != count(db, newTable)) {
            throw new IllegalStateException("Saved record count changed during migration.");
        }
    }

    private static long count(SupportSQLiteDatabase db, String table) {
        try (Cursor cursor = db.query("SELECT COUNT(*) FROM " + table)) {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }
    }

    private static long sequence(SupportSQLiteDatabase db, String table) {
        try (Cursor cursor = db.query(
                "SELECT seq FROM sqlite_sequence WHERE name = '" + table + "'")) {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0;
        }
    }

    private static void restoreSequence(
            SupportSQLiteDatabase db, String table, long previous) {
        db.execSQL(
                "INSERT INTO sqlite_sequence(name,seq) SELECT ?,? " +
                        "WHERE NOT EXISTS(SELECT 1 FROM sqlite_sequence WHERE name = ?)",
                new Object[]{table, previous, table});

        db.execSQL(
                "UPDATE sqlite_sequence SET seq = MAX(seq,?) WHERE name = ?",
                new Object[]{previous, table});
    }
}