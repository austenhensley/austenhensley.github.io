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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.SettingsService;
import com.example.hensley_weighttracker.service.WeightService;
import com.example.hensley_weighttracker.util.WeightUnit;

import java.util.Locale;

public class AddWeightActivity extends AppCompatActivity {

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

        unit = weightService.getPreferredUnit(userId);
        editDailyWeight.setHint(getString(R.string.weight_hint_with_unit, unit.getSuffix()));
        textAddWeightHelp.setText(getString(R.string.enter_weight_help_with_unit, unit.getSuffix()));

        if (weightId != -1) {
            loadWeightForEdit();
        }

        buttonSaveWeight.setOnClickListener(view -> saveWeight());
        buttonCancelWeight.setOnClickListener(view -> finish());
    }

    private void loadWeightForEdit() {
        Double weightValue = weightService.getWeightForDisplay(weightId, userId);
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
        ServiceResult<Double> result = weightService.saveWeight(
                userId,
                weightId,
                editDailyWeight.getText().toString()
        );

        if (!result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        maybeSendGoalReachedSms(result.getValue());
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void maybeSendGoalReachedSms(double currentWeightPounds) {
        if (!weightService.hasReachedGoal(userId, currentWeightPounds)) {
            return;
        }

        UserSettings settings = settingsService.getSettings(userId);
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
