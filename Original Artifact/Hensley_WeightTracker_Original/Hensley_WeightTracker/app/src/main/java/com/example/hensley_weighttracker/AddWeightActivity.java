package com.example.hensley_weighttracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddWeightActivity extends AppCompatActivity {

    private EditText editDailyWeight;
    private TextView textAddWeightTitle;
    private TextView textAddWeightHelp;
    private Button buttonSaveWeight;
    private Button buttonCancelWeight;
    private DBHelper dbHelper;

    private int userId;
    private int weightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);
        weightId = getIntent().getIntExtra("WEIGHT_ID", -1);

        editDailyWeight = findViewById(R.id.editDailyWeight);
        textAddWeightTitle = findViewById(R.id.textAddWeightTitle);
        textAddWeightHelp = findViewById(R.id.textAddWeightHelp);
        buttonSaveWeight = findViewById(R.id.buttonSaveWeight);
        buttonCancelWeight = findViewById(R.id.buttonCancelWeight);

        if (weightId != -1) {
            loadWeightForEdit();
        }

        buttonSaveWeight.setOnClickListener(view -> saveWeight());
        buttonCancelWeight.setOnClickListener(view -> finish());
    }

    private void loadWeightForEdit() {
        Cursor cursor = dbHelper.getWeightById(weightId, userId);

        if (cursor.moveToFirst()) {
            final double weightValue = cursor.getDouble(2);
            editDailyWeight.setText(String.valueOf(weightValue));
            textAddWeightTitle.setText(R.string.update_daily_weight);
            textAddWeightHelp.setText(R.string.edit_selected_weight_entry);
            buttonSaveWeight.setText(R.string.update_weight);
        }

        cursor.close();
    }

    private void saveWeight() {
        String weightText = editDailyWeight.getText().toString().trim();

        if (weightText.isEmpty()) {
            Toast.makeText(this, R.string.enter_weight_value, Toast.LENGTH_SHORT).show();
            return;
        }

        double weightValue = Double.parseDouble(weightText);
        boolean success;

        if (weightId == -1) {
            String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
            success = dbHelper.addWeight(userId, currentDate, weightValue);
        } else {
            String existingDate = dbHelper.getWeightDate(weightId, userId);
            success = dbHelper.updateWeight(weightId, userId, existingDate, weightValue);
        }

        if (success) {
            maybeSendGoalReachedSms(weightValue);

            if (weightId == -1) {
                Toast.makeText(this, R.string.weight_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.weight_updated, Toast.LENGTH_SHORT).show();
            }

            finish();
        } else {
            Toast.makeText(this, R.string.unable_to_save_weight, Toast.LENGTH_SHORT).show();
        }
    }

    private void maybeSendGoalReachedSms(double currentWeight) {
        Double goalWeight = dbHelper.getGoalWeight(userId);
        String phoneNumber = dbHelper.getPhoneNumber(userId);
        int smsEnabled = dbHelper.isSmsEnabled(userId);

        if (goalWeight == null) {
            return;
        }

        if (currentWeight > goalWeight) {
            return;
        }

        if (smsEnabled != 1 || phoneNumber.isEmpty()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            SmsManager smsManager;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            if (smsManager != null) {
                smsManager.sendTextMessage(
                        phoneNumber,
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