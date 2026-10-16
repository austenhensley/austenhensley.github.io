package com.example.hensley_weighttracker.util;

public enum WeightUnit {
    LB("lb"),
    KG("kg");

    private static final double POUNDS_PER_KILOGRAM = 2.2046226218;
    private final String suffix;

    WeightUnit(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public double toPounds(double value) {
        return this == KG ? value * POUNDS_PER_KILOGRAM : value;
    }

    public double fromPounds(double pounds) {
        return this == KG ? pounds / POUNDS_PER_KILOGRAM : pounds;
    }

    public static WeightUnit fromDatabaseValue(String value) {
        if (value == null) {
            return LB;
        }
        try {
            return WeightUnit.valueOf(value);
        } catch (IllegalArgumentException e) {
            return LB;
        }
    }
}
