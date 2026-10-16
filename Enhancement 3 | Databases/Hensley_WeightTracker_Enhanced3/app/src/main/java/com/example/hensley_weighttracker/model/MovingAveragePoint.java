package com.example.hensley_weighttracker.model;

/** A complete rolling window, associated with its newest entry. */
public final class MovingAveragePoint {
    private final int entryId;
    private final String date;
    private final double averagePounds;

    public MovingAveragePoint(int entryId, String date, double averagePounds) {
        this.entryId = entryId;
        this.date = date;
        this.averagePounds = averagePounds;
    }

    public int getEntryId() { return entryId; }
    public String getDate() { return date; }
    public double getAveragePounds() { return averagePounds; }
}
