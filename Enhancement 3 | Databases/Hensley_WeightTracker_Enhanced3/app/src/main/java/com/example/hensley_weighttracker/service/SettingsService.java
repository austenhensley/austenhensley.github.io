package com.example.hensley_weighttracker.service;

import android.content.Context;

import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.repository.SettingsRepository;
import com.example.hensley_weighttracker.util.InputValidator;
import com.example.hensley_weighttracker.util.ValidationResult;
import com.example.hensley_weighttracker.util.WeightUnit;

public class SettingsService {
    private final SettingsRepository settingsRepository;

    public SettingsService(Context context) {
        settingsRepository = new SettingsRepository(context);
    }

    public UserSettings getSettings(int userId) {
        return settingsRepository.getSettings(userId);
    }

    public boolean saveWeightUnit(int userId, WeightUnit unit) {
        return settingsRepository.saveWeightUnit(userId, unit);
    }

    public ServiceResult<Void> saveNotificationSettings(int userId, String phoneNumber, boolean enabled) {
        ValidationResult validation = InputValidator.validatePhoneNumber(phoneNumber);
        if (!validation.isValid()) {
            return ServiceResult.failure(validation.getMessage());
        }

        boolean saved = settingsRepository.saveNotificationSettings(userId, phoneNumber.trim(), enabled);
        return saved
                ? ServiceResult.success(null, "Notification settings saved.")
                : ServiceResult.failure("Unable to save notification settings.");
    }
}
