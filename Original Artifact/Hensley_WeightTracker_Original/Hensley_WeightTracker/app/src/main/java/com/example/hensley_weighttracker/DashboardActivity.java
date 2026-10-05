package com.example.hensley_weighttracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private int userId;
    private DBHelper dbHelper;
    private TextView textGoalValue;
    private TableLayout tableWeightHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, R.string.login_session_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DBHelper(this);
        textGoalValue = findViewById(R.id.textGoalValue);
        tableWeightHistory = findViewById(R.id.tableWeightHistory);

        Button buttonAddWeight = findViewById(R.id.buttonAddWeight);
        Button buttonSetGoal = findViewById(R.id.buttonSetGoal);
        Button buttonNotifications = findViewById(R.id.buttonNotifications);

        buttonAddWeight.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddWeightActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        buttonSetGoal.setOnClickListener(view -> {
            Intent intent = new Intent(this, GoalWeightActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        buttonNotifications.setOnClickListener(view -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoalWeight();
        loadWeightTable();
    }

    private void loadGoalWeight() {
        Double goalWeight = dbHelper.getGoalWeight(userId);

        if (goalWeight == null) {
            textGoalValue.setText(R.string.no_goal_set);
        } else {
            textGoalValue.setText(getString(R.string.goal_weight_display, goalWeight));
        }
    }

    private void loadWeightTable() {
        while (tableWeightHistory.getChildCount() > 1) {
            tableWeightHistory.removeViewAt(1);
        }

        Cursor cursor = dbHelper.getAllWeights(userId);

        if (cursor.getCount() == 0) {
            TableRow emptyRow = new TableRow(this);

            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.no_weight_entries_yet);
            emptyText.setPadding(16, 16, 16, 16);

            emptyRow.addView(emptyText);
            tableWeightHistory.addView(emptyRow);
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            final int weightId = cursor.getInt(0);
            String date = cursor.getString(1);
            double weightValue = cursor.getDouble(2);

            TableRow row = new TableRow(this);

            TextView dateText = new TextView(this);
            dateText.setText(date);
            dateText.setPadding(8, 8, 8, 8);

            TextView weightText = new TextView(this);
            weightText.setText(getString(R.string.weight_value_display, weightValue));
            weightText.setPadding(8, 8, 8, 8);

            Button editButton = new Button(this);
            editButton.setText(R.string.edit_button_text);
            editButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, AddWeightActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("WEIGHT_ID", weightId);
                startActivity(intent);
            });

            Button deleteButton = new Button(this);
            deleteButton.setText(R.string.delete_button_text);
            deleteButton.setOnClickListener(view -> {
                boolean deleted = dbHelper.deleteWeight(weightId, userId);

                if (deleted) {
                    Toast.makeText(this, R.string.weight_entry_deleted, Toast.LENGTH_SHORT).show();
                    loadWeightTable();
                } else {
                    Toast.makeText(this, R.string.unable_to_delete_entry, Toast.LENGTH_SHORT).show();
                }
            });

            row.addView(dateText);
            row.addView(weightText);
            row.addView(editButton);
            row.addView(deleteButton);

            tableWeightHistory.addView(row);
        }

        cursor.close();
    }
}