package com.example.hensley_weighttracker.model;

import com.example.hensley_weighttracker.util.WeightUnit;

public class UserSettings {
    private final Double goalWeightPounds;
    private final String phoneNumber;
    private final boolean smsEnabled;
    private final WeightUnit weightUnit;

    public UserSettings(Double goalWeightPounds, String phoneNumber, boolean smsEnabled, WeightUnit weightUnit) {
        this.goalWeightPounds = goalWeightPounds;
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
        this.smsEnabled = smsEnabled;
        this.weightUnit = weightUnit == null ? WeightUnit.LB : weightUnit;
    }

    public Double getGoalWeightPounds() {
        return goalWeightPounds;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public WeightUnit getWeightUnit() {
        return weightUnit;
    }
}
