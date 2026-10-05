package com.example.hensley_weighttracker.service;

import com.example.hensley_weighttracker.model.MovingAveragePoint;
import com.example.hensley_weighttracker.model.WeightEntry;
import com.example.hensley_weighttracker.model.WeightTrendSummary;
import com.example.hensley_weighttracker.model.WeightTrendSummary.Trend;
import com.example.hensley_weighttracker.util.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java analysis of the existing stored pounds values.
 * Sort: O(n log n). Sliding-window pass: O(n) time, O(k) queue space.
 * The sorted copy and retained average series make total space O(n + k).
 * Here k = 7 entries, not seven calendar days. No missing days are invented.
 */
public final class WeightTrendAnalyzer {
    public static final int WINDOW_SIZE = 7;
    // Ignore tiny differences caused by decimal calculations.
    private static final double EPSILON = 1.0e-9;
    private static final DateTimeFormatter STORED_DATE = DateTimeFormatter
            .ofPattern("MM/dd/uuuu", Locale.US).withResolverStyle(ResolverStyle.STRICT);

    public WeightTrendSummary analyze(List<WeightEntry> entries, Double goalPounds) {
        if (entries == null) {
            throw new IllegalArgumentException("Weight history is missing.");
        }
        if (goalPounds != null && !InputValidator.validateStoredWeightPounds(goalPounds).isValid()) {
            throw new IllegalArgumentException("The saved goal contains an invalid weight.");
        }

        // Parse once per record, outside the comparator. Never change the caller's list.
        List<DatedEntry> ordered = new ArrayList<>();
        for (WeightEntry entry : entries) {
            if (entry == null || entry.getDate() == null
                    || !InputValidator.validateStoredWeightPounds(entry.getWeightPounds()).isValid()) {
                throw new IllegalArgumentException("Weight history contains an invalid entry.");
            }
            try {
                ordered.add(new DatedEntry(entry, LocalDate.parse(entry.getDate(), STORED_DATE)));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException("Weight history contains an invalid date.");
            }
        }
        // Keep entries from the same day separate and order them by entry ID.
        ordered.sort(Comparator.comparing((DatedEntry item) -> item.date)
                .thenComparingInt(item -> item.entry.getId()));

        List<MovingAveragePoint> averages = new ArrayList<>();
        Deque<WeightEntry> window = new ArrayDeque<>(WINDOW_SIZE);
        double runningTotal = 0.0;
        for (DatedEntry item : ordered) {
            if (window.size() == WINDOW_SIZE) {
                runningTotal -= window.removeFirst().getWeightPounds();
            }
            window.addLast(item.entry);
            runningTotal += item.entry.getWeightPounds();
            if (window.size() == WINDOW_SIZE) {
                averages.add(new MovingAveragePoint(item.entry.getId(), item.entry.getDate(),
                        runningTotal / WINDOW_SIZE));
            }
        }

        Trend trend = Trend.INSUFFICIENT_DATA;
        if (averages.size() >= 2) {
            double change = averages.get(averages.size() - 1).getAveragePounds()
                    - averages.get(averages.size() - 2).getAveragePounds();
            trend = Math.abs(change) <= EPSILON ? Trend.STABLE
                    : change > 0 ? Trend.INCREASING : Trend.DECREASING;
        }
        if (ordered.isEmpty()) {
            return new WeightTrendSummary(0, null, null, null, averages, trend);
        }

        double startingWeight = ordered.get(0).entry.getWeightPounds();
        double currentWeight = ordered.get(ordered.size() - 1).entry.getWeightPounds();
        Double progress = null;
        Double distance = null;
        if (goalPounds != null) {
            distance = Math.abs(currentWeight - goalPounds);
            double plannedChange = goalPounds - startingWeight;
            if (Math.abs(plannedChange) > EPSILON) {
                // Signed numerator/denominator supports both gain and loss goals.
                double percent = (currentWeight - startingWeight) / plannedChange * 100.0;
                progress = Math.max(0.0, Math.min(100.0, percent));
            }
            // Avoid dividing by zero when the starting weight already matches the goal.
            // The dashboard still displays the current distance from the goal.
        }
        return new WeightTrendSummary(ordered.size(), currentWeight - startingWeight,
                progress, distance, averages, trend);
    }

    private static final class DatedEntry {
        private final WeightEntry entry;
        private final LocalDate date;

        private DatedEntry(WeightEntry entry, LocalDate date) {
            this.entry = entry;
            this.date = date;
        }
    }
}
