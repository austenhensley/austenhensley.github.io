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
import androidx.core.content.ContextCompat;
import com.example.hensley_weighttracker.service.SettingsService;

public class NotificationActivity extends DatabaseActivity {
    private EditText editPhoneNumber;
    private TextView textSmsStatus;
    private Button buttonSave, buttonEnable;
    private SettingsService settingsService;
    private int userId;
    private String permissionPhone;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Persist the number that was validated before the permission dialog opened.
                if (permissionPhone == null) return;
                savePermissionResult(permissionPhone, granted);
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_notification);
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) { finish(); return; }
        if (state != null) permissionPhone = state.getString("PERMISSION_PHONE");
        settingsService = new SettingsService(this);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        buttonSave = findViewById(R.id.buttonSaveSmsSettings);
        buttonEnable = findViewById(R.id.buttonEnableSms);
        databaseTask(() -> settingsService.getSettings(userId), settings -> {
            editPhoneNumber.setText(settings.getPhoneNumber());
            updatePermissionStatusText();
        }, buttonSave, buttonEnable, editPhoneNumber);
        buttonSave.setOnClickListener(v -> savePhoneNumberOnly());
        buttonEnable.setOnClickListener(v -> requestSmsPermission());
        findViewById(R.id.buttonBackFromSms).setOnClickListener(v -> finish());
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("PERMISSION_PHONE", permissionPhone);
    }

    private void savePhoneNumberOnly() {
        String phone = editPhoneNumber.getText().toString().trim();
        databaseTask(() -> settingsService.saveNotificationSettings(userId, phone,
                settingsService.getSettings(userId).isSmsEnabled()), result ->
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show(),
                buttonSave, buttonEnable, editPhoneNumber);
    }

    private void requestSmsPermission() {
        String phone = editPhoneNumber.getText().toString().trim();
        databaseTask(() -> settingsService.saveNotificationSettings(userId, phone,
                settingsService.getSettings(userId).isSmsEnabled()), result -> {
            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            permissionPhone = phone;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                savePermissionResult(phone, true);
            } else {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            }
        }, buttonSave, buttonEnable, editPhoneNumber);
    }

    private void savePermissionResult(String phone, boolean granted) {
        databaseTask(() -> settingsService.saveNotificationSettings(userId, phone, granted), result -> {
            permissionPhone = null;
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            if (result.isSuccess()) textSmsStatus.setText(granted
                    ? R.string.sms_enabled_message : R.string.sms_denied_message);
        }, buttonSave, buttonEnable, editPhoneNumber);
    }

    private void updatePermissionStatusText() {
        textSmsStatus.setText(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED ? R.string.sms_permission_granted_short
                : R.string.sms_permission_not_granted);
    }
}
