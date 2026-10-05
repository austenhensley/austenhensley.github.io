package com.example.hensley_weighttracker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores the trend results. Null values mean there is not enough data to calculate a result. */
public final class WeightTrendSummary {
    public enum Trend { INCREASING, DECREASING, STABLE, INSUFFICIENT_DATA }

    private final int entryCount;
    private final Double totalChangePounds;
    private final Double goalProgressPercent;
    private final Double goalDistancePounds;
    private final List<MovingAveragePoint> movingAverages;
    private final Trend trend;

    public WeightTrendSummary(int entryCount, Double totalChangePounds,
                              Double goalProgressPercent, Double goalDistancePounds,
                              List<MovingAveragePoint> movingAverages, Trend trend) {
        this.entryCount = entryCount;
        this.totalChangePounds = totalChangePounds;
        this.goalProgressPercent = goalProgressPercent;
        this.goalDistancePounds = goalDistancePounds;
        this.movingAverages = Collections.unmodifiableList(new ArrayList<>(movingAverages));
        this.trend = trend;
    }

    public int getEntryCount() { return entryCount; }
    public Double getTotalChangePounds() { return totalChangePounds; }
    public Double getGoalProgressPercent() { return goalProgressPercent; }
    public Double getGoalDistancePounds() { return goalDistancePounds; }
    public List<MovingAveragePoint> getMovingAverages() { return movingAverages; }
    public Trend getTrend() { return trend; }

    public Double getLatestAveragePounds() {
        return movingAverages.isEmpty() ? null
                : movingAverages.get(movingAverages.size() - 1).getAveragePounds();
    }
}
