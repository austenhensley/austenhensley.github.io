package com.example.hensley_weighttracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.SettingsService;
import com.example.hensley_weighttracker.service.WeightService;
import com.example.hensley_weighttracker.util.WeightUnit;

import java.util.Locale;

public class AddWeightActivity extends DatabaseActivity {

    private EditText editDailyWeight;
    private TextView textAddWeightTitle;
    private TextView textAddWeightHelp;
    private Button buttonSaveWeight;
    private WeightService weightService;
    private SettingsService settingsService;
    private int userId;
    private int weightId;
    private WeightUnit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        weightService = new WeightService(this);
        settingsService = new SettingsService(this);
        userId = getIntent().getIntExtra("USER_ID", -1);
        weightId = getIntent().getIntExtra("WEIGHT_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, R.string.login_session_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editDailyWeight = findViewById(R.id.editDailyWeight);
        textAddWeightTitle = findViewById(R.id.textAddWeightTitle);
        textAddWeightHelp = findViewById(R.id.textAddWeightHelp);
        buttonSaveWeight = findViewById(R.id.buttonSaveWeight);
        Button buttonCancelWeight = findViewById(R.id.buttonCancelWeight);

        databaseTask(() -> {
            unit = weightService.getPreferredUnit(userId);
            return weightId == -1 ? null : weightService.getWeightForDisplay(weightId, userId);
        }, value -> {
            editDailyWeight.setHint(getString(R.string.weight_hint_with_unit, unit.getSuffix()));
            textAddWeightHelp.setText(getString(R.string.enter_weight_help_with_unit, unit.getSuffix()));
            if (weightId != -1) loadWeightForEdit(value);
        }, buttonSaveWeight, editDailyWeight);

        buttonSaveWeight.setOnClickListener(view -> saveWeight());
        buttonCancelWeight.setOnClickListener(view -> finish());
    }

    private void loadWeightForEdit(Double weightValue) {
        if (weightValue == null) {
            Toast.makeText(this, R.string.weight_entry_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editDailyWeight.setText(String.format(Locale.US, "%.1f", weightValue));
        textAddWeightTitle.setText(R.string.update_daily_weight);
        textAddWeightHelp.setText(getString(R.string.edit_weight_help_with_unit, unit.getSuffix()));
        buttonSaveWeight.setText(R.string.update_weight);
    }

    private void saveWeight() {
        String entered = editDailyWeight.getText().toString();
        databaseTask(() -> {
            ServiceResult<Double> result = weightService.saveWeight(userId, weightId, entered);
            UserSettings sms = null;
            if (result.isSuccess() && weightService.hasReachedGoal(userId, result.getValue())) {
                sms = settingsService.getSettings(userId);
            }
            return new SaveResult(result, sms);
        }, saved -> {
            Toast.makeText(this, saved.result.getMessage(), Toast.LENGTH_SHORT).show();
            if (saved.result.isSuccess()) {
                if (saved.sms != null) maybeSendGoalReachedSms(saved.sms);
                finish();
            }
        }, buttonSaveWeight, editDailyWeight);
    }

    private static class SaveResult {
        final ServiceResult<Double> result;
        final UserSettings sms;
        SaveResult(ServiceResult<Double> result, UserSettings sms) {
            this.result = result; this.sms = sms;
        }
    }

    private void maybeSendGoalReachedSms(UserSettings settings) {
        if (!settings.isSmsEnabled() || settings.getPhoneNumber().isEmpty()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            SmsManager smsManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? getSystemService(SmsManager.class)
                    : SmsManager.getDefault();

            if (smsManager != null) {
                smsManager.sendTextMessage(
                        settings.getPhoneNumber(),
                        null,
                        getString(R.string.goal_reached_message),
                        null,
                        null
                );
                Toast.makeText(this, R.string.goal_reached_sms_sent, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.goal_reached_sms_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
