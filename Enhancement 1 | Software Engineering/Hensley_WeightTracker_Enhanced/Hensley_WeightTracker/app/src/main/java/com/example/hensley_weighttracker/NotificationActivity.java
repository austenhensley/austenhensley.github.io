package com.example.hensley_weighttracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hensley_weighttracker.model.UserSettings;
import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.SettingsService;

public class NotificationActivity extends AppCompatActivity {

    private EditText editPhoneNumber;
    private TextView textSmsStatus;
    private SettingsService settingsService;
    private int userId;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                String phoneNumber = editPhoneNumber.getText().toString().trim();
                ServiceResult<Void> result = settingsService.saveNotificationSettings(
                        userId,
                        phoneNumber,
                        isGranted
                );

                if (!result.isSuccess()) {
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isGranted) {
                    textSmsStatus.setText(R.string.sms_enabled_message);
                    Toast.makeText(this, R.string.sms_granted_toast, Toast.LENGTH_SHORT).show();
                } else {
                    textSmsStatus.setText(R.string.sms_denied_message);
                    Toast.makeText(this, R.string.sms_denied_toast, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, R.string.login_session_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        settingsService = new SettingsService(this);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        Button buttonSaveSmsSettings = findViewById(R.id.buttonSaveSmsSettings);
        Button buttonEnableSms = findViewById(R.id.buttonEnableSms);
        Button buttonBackFromSms = findViewById(R.id.buttonBackFromSms);

        loadSavedSettings();
        updatePermissionStatusText();

        buttonSaveSmsSettings.setOnClickListener(view -> savePhoneNumberOnly());
        buttonEnableSms.setOnClickListener(view -> requestSmsPermission());
        buttonBackFromSms.setOnClickListener(view -> finish());
    }

    private void loadSavedSettings() {
        String phoneNumber = settingsService.getSettings(userId).getPhoneNumber();
        if (!phoneNumber.isEmpty()) {
            editPhoneNumber.setText(phoneNumber);
        }
    }

    private void savePhoneNumberOnly() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        UserSettings settings = settingsService.getSettings(userId);
        ServiceResult<Void> result = settingsService.saveNotificationSettings(
                userId,
                phoneNumber,
                settings.isSmsEnabled()
        );
        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void requestSmsPermission() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        UserSettings settings = settingsService.getSettings(userId);

        ServiceResult<Void> validationAndSave = settingsService.saveNotificationSettings(
                userId,
                phoneNumber,
                settings.isSmsEnabled()
        );
        if (!validationAndSave.isSuccess()) {
            Toast.makeText(this, validationAndSave.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            ServiceResult<Void> result = settingsService.saveNotificationSettings(userId, phoneNumber, true);
            if (result.isSuccess()) {
                textSmsStatus.setText(R.string.sms_already_enabled);
                Toast.makeText(this, R.string.sms_already_enabled_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    private void updatePermissionStatusText() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            textSmsStatus.setText(R.string.sms_permission_granted_short);
        } else {
            textSmsStatus.setText(R.string.sms_permission_not_granted);
        }
    }
}
