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

public class NotificationActivity extends AppCompatActivity {

    private EditText editPhoneNumber;
    private TextView textSmsStatus;
    private Button buttonSaveSmsSettings;
    private Button buttonEnableSms;
    private Button buttonBackFromSms;

    private DBHelper dbHelper;
    private int userId;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                String phoneNumber = editPhoneNumber.getText().toString().trim();

                if (isGranted) {
                    dbHelper.saveNotificationSettings(userId, phoneNumber, 1);
                    textSmsStatus.setText(R.string.sms_enabled_message);
                    Toast.makeText(this, R.string.sms_granted_toast, Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.saveNotificationSettings(userId, phoneNumber, 0);
                    textSmsStatus.setText(R.string.sms_denied_message);
                    Toast.makeText(this, R.string.sms_denied_toast, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);

        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        buttonSaveSmsSettings = findViewById(R.id.buttonSaveSmsSettings);
        buttonEnableSms = findViewById(R.id.buttonEnableSms);
        buttonBackFromSms = findViewById(R.id.buttonBackFromSms);

        loadSavedSettings();
        updatePermissionStatusText();

        buttonSaveSmsSettings.setOnClickListener(view -> savePhoneNumberOnly());
        buttonEnableSms.setOnClickListener(view -> requestSmsPermission());
        buttonBackFromSms.setOnClickListener(view -> finish());
    }

    private void loadSavedSettings() {
        String phoneNumber = dbHelper.getPhoneNumber(userId);

        if (!phoneNumber.isEmpty()) {
            editPhoneNumber.setText(phoneNumber);
        }
    }

    private void savePhoneNumberOnly() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        int currentEnabledValue = dbHelper.isSmsEnabled(userId);

        dbHelper.saveNotificationSettings(userId, phoneNumber, currentEnabledValue);
        Toast.makeText(this, R.string.notification_settings_saved, Toast.LENGTH_SHORT).show();
    }

    private void requestSmsPermission() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, R.string.enter_phone_number_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            dbHelper.saveNotificationSettings(userId, phoneNumber, 1);
            textSmsStatus.setText(R.string.sms_already_enabled);
            Toast.makeText(this, R.string.sms_already_enabled_toast, Toast.LENGTH_SHORT).show();
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