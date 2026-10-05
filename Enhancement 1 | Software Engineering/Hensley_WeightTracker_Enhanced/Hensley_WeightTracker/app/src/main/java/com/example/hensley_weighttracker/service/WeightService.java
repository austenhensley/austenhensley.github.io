package com.example.hensley_weighttracker.service;

import android.content.Context;

import com.example.hensley_weighttracker.model.DisplayWeightEntry;
import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.model.WeightEntry;
import com.example.hensley_weighttracker.repository.SettingsRepository;
import com.example.hensley_weighttracker.repository.WeightRepository;
import com.example.hensley_weighttracker.util.InputValidator;
import com.example.hensley_weighttracker.util.ValidationResult;
import com.example.hensley_weighttracker.util.WeightUnit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightService {
    private final WeightRepository weightRepository;
    private final SettingsRepository settingsRepository;

    public WeightService(Context context) {
        weightRepository = new WeightRepository(context);
        settingsRepository = new SettingsRepository(context);
    }

    public WeightUnit getPreferredUnit(int userId) {
        return settingsRepository.getSettings(userId).getWeightUnit();
    }

    public List<DisplayWeightEntry> getWeightHistory(int userId) {
        WeightUnit unit = getPreferredUnit(userId);
        List<DisplayWeightEntry> displayEntries = new ArrayList<>();

        for (WeightEntry entry : weightRepository.getAllWeights(userId)) {
            displayEntries.add(new DisplayWeightEntry(
                    entry.getId(),
                    entry.getDate(),
                    unit.fromPounds(entry.getWeightPounds())
            ));
        }
        return displayEntries;
    }

    public Double getWeightForDisplay(int weightId, int userId) {
        WeightEntry entry = weightRepository.getWeightById(weightId, userId);
        if (entry == null) {
            return null;
        }
        return getPreferredUnit(userId).fromPounds(entry.getWeightPounds());
    }

    public ServiceResult<Double> saveWeight(int userId, int weightId, String weightText) {
        ValidationResult textValidation = InputValidator.validateWeightText(weightText);
        if (!textValidation.isValid()) {
            return ServiceResult.failure(textValidation.getMessage());
        }

        double enteredValue = Double.parseDouble(weightText.trim());
        double pounds = getPreferredUnit(userId).toPounds(enteredValue);
        ValidationResult rangeValidation = InputValidator.validateStoredWeightPounds(pounds);
        if (!rangeValidation.isValid()) {
            return ServiceResult.failure(rangeValidation.getMessage());
        }

        boolean success;
        if (weightId == -1) {
            String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
            success = weightRepository.addWeight(userId, currentDate, pounds);
        } else {
            WeightEntry existing = weightRepository.getWeightById(weightId, userId);
            if (existing == null) {
                return ServiceResult.failure("Weight entry was not found.");
            }
            success = weightRepository.updateWeight(weightId, userId, existing.getDate(), pounds);
        }

        return success
                ? ServiceResult.success(pounds, weightId == -1 ? "Weight saved." : "Weight updated.")
                : ServiceResult.failure("Unable to save weight.");
    }

    public boolean deleteWeight(int weightId, int userId) {
        return weightRepository.deleteWeight(weightId, userId);
    }

    public Double getGoalForDisplay(int userId) {
        UserSettings settings = settingsRepository.getSettings(userId);
        if (settings.getGoalWeightPounds() == null) {
            return null;
        }
        return settings.getWeightUnit().fromPounds(settings.getGoalWeightPounds());
    }

    public ServiceResult<Double> saveGoal(int userId, String goalText) {
        ValidationResult textValidation = InputValidator.validateWeightText(goalText);
        if (!textValidation.isValid()) {
            return ServiceResult.failure(textValidation.getMessage());
        }

        double enteredValue = Double.parseDouble(goalText.trim());
        WeightUnit unit = getPreferredUnit(userId);
        double pounds = unit.toPounds(enteredValue);
        ValidationResult rangeValidation = InputValidator.validateStoredWeightPounds(pounds);
        if (!rangeValidation.isValid()) {
            return ServiceResult.failure(rangeValidation.getMessage());
        }

        boolean saved = settingsRepository.saveGoalWeight(userId, pounds);
        return saved
                ? ServiceResult.success(pounds, "Goal weight saved.")
                : ServiceResult.failure("Unable to save goal weight.");
    }

    public boolean hasReachedGoal(int userId, double currentWeightPounds) {
        Double goalPounds = settingsRepository.getSettings(userId).getGoalWeightPounds();
        return goalPounds != null && currentWeightPounds <= goalPounds;
    }
}
