package com.example.hensley_weighttracker.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import com.example.hensley_weighttracker.data.AppDatabase;
import com.example.hensley_weighttracker.data.dao.WeightDao;
import com.example.hensley_weighttracker.data.entity.WeightEntity;
import com.example.hensley_weighttracker.model.WeightEntry;
import com.example.hensley_weighttracker.util.DateCodec;
import java.util.ArrayList;
import java.util.List;

public class WeightRepository {
    private final WeightDao weights;

    public WeightRepository(Context context) { weights = AppDatabase.getInstance(context).weights(); }

    public boolean addWeight(int userId, String date, double pounds) {
        WeightEntity entry = new WeightEntity();
        entry.userId = userId;
        entry.date = DateCodec.toStored(date);
        entry.pounds = pounds;
        try { return weights.insert(entry) > 0; }
        catch (SQLiteConstraintException exception) { return false; }
    }

    public boolean updateWeight(int id, int userId, String date, double pounds) {
        try { return weights.update(id, userId, DateCodec.toStored(date), pounds) == 1; }
        catch (SQLiteConstraintException exception) { return false; }
    }

    public boolean deleteWeight(int id, int userId) { return weights.delete(id, userId) == 1; }

    public WeightEntry getWeightById(int id, int userId) {
        WeightEntity entry = weights.find(id, userId);
        return entry == null ? null : toModel(entry);
    }

    public List<WeightEntry> getAllWeights(int userId) { return toModels(weights.history(userId)); }

    public List<WeightEntry> getWeightsBetween(int userId, String start, String end) {
        String from = DateCodec.toStored(start);
        String through = DateCodec.toStored(end);
        if (from.compareTo(through) > 0) throw new IllegalArgumentException("Start date must be before the end date.");
        return toModels(weights.historyBetween(userId, from, through));
    }

    private WeightEntry toModel(WeightEntity entry) {
        return new WeightEntry(entry.id, entry.date, entry.pounds);
    }

    private List<WeightEntry> toModels(List<WeightEntity> records) {
        List<WeightEntry> entries = new ArrayList<>();
        for (WeightEntity entry : records) entries.add(toModel(entry));
        return entries;
    }
}
