package com.example.hensley_weighttracker.model;

public class DisplayWeightEntry {
    private final int id;
    private final String date;
    private final double displayWeight;

    public DisplayWeightEntry(int id, String date, double displayWeight) {
        this.id = id;
        this.date = date;
        this.displayWeight = displayWeight;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getDisplayWeight() {
        return displayWeight;
    }
}
