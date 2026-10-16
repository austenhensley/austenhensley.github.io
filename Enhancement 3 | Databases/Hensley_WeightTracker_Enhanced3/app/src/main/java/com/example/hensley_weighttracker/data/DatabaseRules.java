package com.example.hensley_weighttracker.data;

import androidx.sqlite.db.SupportSQLiteDatabase;

public final class DatabaseRules {
    private DatabaseRules() { }

    public static void install(SupportSQLiteDatabase db) {
        // Room annotations define relationships; triggers enforce value rules in SQLite itself.
        String validDate = "NEW.weight_date GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' " +
                "AND substr(NEW.weight_date, 1, 4) BETWEEN '0001' AND '9999' " +
                "AND date(NEW.weight_date, '+0 days') IS NOT NULL " +
                "AND date(NEW.weight_date, '+0 days') = NEW.weight_date";
        String validWeight = "typeof(NEW.weight_value) IN ('real','integer') " +
                "AND NEW.weight_value BETWEEN 1 AND 1500";
        String validGoal = "typeof(NEW.goal_weight) IN ('real','integer') " +
                "AND NEW.goal_weight BETWEEN 1 AND 1500";
        for (String operation : new String[]{"INSERT", "UPDATE"}) {
            addTrigger(db, "weights", operation, "(" + validDate + ") AND (" + validWeight + ")");
            addTrigger(db, "goals", operation, validGoal);
            addTrigger(db, "settings", operation,
                    "NEW.sms_enabled IN (0,1) AND NEW.weight_unit IN ('LB','KG')");
        }
    }

    private static void addTrigger(SupportSQLiteDatabase db, String table,
                                   String operation, String condition) {
        db.execSQL("CREATE TRIGGER IF NOT EXISTS validate_" + table + "_" + operation +
                " BEFORE " + operation + " ON " + table +
                " WHEN NOT (" + condition + ") BEGIN " +
                "SELECT RAISE(ABORT, 'Invalid stored value'); END");
    }
}
