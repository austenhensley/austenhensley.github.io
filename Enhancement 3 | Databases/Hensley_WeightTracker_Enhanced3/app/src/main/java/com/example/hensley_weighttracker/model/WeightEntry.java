package com.example.hensley_weighttracker.model;

public class WeightEntry {
    private final int id;
    private final String date;
    private final double weightPounds;

    public WeightEntry(int id, String date, double weightPounds) {
        this.id = id;
        this.date = date;
        this.weightPounds = weightPounds;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeightPounds() {
        return weightPounds;
    }
}
