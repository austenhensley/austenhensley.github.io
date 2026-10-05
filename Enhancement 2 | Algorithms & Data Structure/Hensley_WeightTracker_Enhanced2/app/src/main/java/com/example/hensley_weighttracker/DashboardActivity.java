package com.example.hensley_weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hensley_weighttracker.model.DisplayWeightEntry;
import com.example.hensley_weighttracker.model.WeightTrendSummary;
import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.SettingsService;
import com.example.hensley_weighttracker.service.WeightService;
import com.example.hensley_weighttracker.util.WeightUnit;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private int userId;
    private WeightService weightService;
    private SettingsService settingsService;
    private TextView textGoalValue;
    private TextView textWeightHeader;
    private TextView textTrendSummary;
    private TableLayout tableWeightHistory;
    private Spinner spinnerWeightUnit;
    private boolean initializingUnitSpinner;

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

        weightService = new WeightService(this);
        settingsService = new SettingsService(this);
        textGoalValue = findViewById(R.id.textGoalValue);
        textWeightHeader = findViewById(R.id.textWeightHeader);
        textTrendSummary = findViewById(R.id.textTrendSummary);
        tableWeightHistory = findViewById(R.id.tableWeightHistory);
        spinnerWeightUnit = findViewById(R.id.spinnerWeightUnit);

        Button buttonAddWeight = findViewById(R.id.buttonAddWeight);
        Button buttonSetGoal = findViewById(R.id.buttonSetGoal);
        Button buttonNotifications = findViewById(R.id.buttonNotifications);

        configureUnitSpinner();

        buttonAddWeight.setOnClickListener(view -> openActivity(AddWeightActivity.class));
        buttonSetGoal.setOnClickListener(view -> openActivity(GoalWeightActivity.class));
        buttonNotifications.setOnClickListener(view -> openActivity(NotificationActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (weightService == null) {
            return;
        }
        syncUnitSpinner();
        loadGoalWeight();
        loadWeightTable();
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void configureUnitSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.weight_unit_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeightUnit.setAdapter(adapter);
        syncUnitSpinner();

        spinnerWeightUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initializingUnitSpinner) {
                    return;
                }

                WeightUnit selectedUnit = position == 1 ? WeightUnit.KG : WeightUnit.LB;
                if (settingsService.saveWeightUnit(userId, selectedUnit)) {
                    loadGoalWeight();
                    loadWeightTable();
                } else {
                    Toast.makeText(DashboardActivity.this, R.string.unable_to_save_unit, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void syncUnitSpinner() {
        if (spinnerWeightUnit == null || weightService == null) {
            return;
        }
        initializingUnitSpinner = true;
        spinnerWeightUnit.setSelection(weightService.getPreferredUnit(userId) == WeightUnit.KG ? 1 : 0);
        initializingUnitSpinner = false;
    }

    private void loadGoalWeight() {
        WeightUnit unit = weightService.getPreferredUnit(userId);
        Double goalWeight = weightService.getGoalForDisplay(userId);

        if (goalWeight == null) {
            textGoalValue.setText(R.string.no_goal_set);
        } else {
            textGoalValue.setText(getString(R.string.goal_weight_display, goalWeight, unit.getSuffix()));
        }
    }

    private void loadWeightTable() {
        // Every history refresh also updates analysis after add/edit/delete or unit changes.
        loadTrendSummary();
        while (tableWeightHistory.getChildCount() > 1) {
            tableWeightHistory.removeViewAt(1);
        }

        WeightUnit unit = weightService.getPreferredUnit(userId);
        textWeightHeader.setText(getString(R.string.weight_header_with_unit, unit.getSuffix()));
        List<DisplayWeightEntry> entries = weightService.getWeightHistory(userId);

        if (entries.isEmpty()) {
            TableRow emptyRow = new TableRow(this);
            TextView emptyText = new TextView(this);
            emptyText.setText(R.string.no_weight_entries_yet);
            emptyText.setPadding(16, 16, 16, 16);
            emptyRow.addView(emptyText);
            tableWeightHistory.addView(emptyRow);
            return;
        }

        for (DisplayWeightEntry entry : entries) {
            TableRow row = new TableRow(this);

            TextView dateText = new TextView(this);
            dateText.setText(entry.getDate());
            dateText.setPadding(8, 8, 8, 8);

            TextView weightText = new TextView(this);
            weightText.setText(getString(R.string.weight_value_display, entry.getDisplayWeight(), unit.getSuffix()));
            weightText.setPadding(8, 8, 8, 8);

            Button editButton = new Button(this);
            editButton.setText(R.string.edit_button_text);
            editButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, AddWeightActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("WEIGHT_ID", entry.getId());
                startActivity(intent);
            });

            Button deleteButton = new Button(this);
            deleteButton.setText(R.string.delete_button_text);
            deleteButton.setOnClickListener(view -> {
                if (weightService.deleteWeight(entry.getId(), userId)) {
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
    }

    private void loadTrendSummary() {
        ServiceResult<WeightTrendSummary> result = weightService.getTrendSummary(userId);
        if (!result.isSuccess()) {
            textTrendSummary.setText(getString(R.string.trend_unavailable, result.getMessage()));
            return;
        }
        WeightTrendSummary summary = result.getValue();
        if (summary.getEntryCount() == 0) {
            textTrendSummary.setText(R.string.trend_no_data);
            return;
        }

        WeightUnit unit = weightService.getPreferredUnit(userId);
        StringBuilder text = new StringBuilder(getString(R.string.trend_total_change,
                unit.fromPounds(summary.getTotalChangePounds()), unit.getSuffix()));
        Double average = summary.getLatestAveragePounds();
        text.append('\n').append(average == null
                ? getString(R.string.trend_need_seven, summary.getEntryCount())
                : getString(R.string.trend_average, unit.fromPounds(average), unit.getSuffix()));

        int trendLabel;
        switch (summary.getTrend()) {
            case INCREASING: trendLabel = R.string.trend_increasing; break;
            case DECREASING: trendLabel = R.string.trend_decreasing; break;
            case STABLE: trendLabel = R.string.trend_stable; break;
            default: trendLabel = R.string.trend_need_eight; break;
        }
        text.append('\n').append(getString(trendLabel));
        if (summary.getGoalDistancePounds() == null) {
            text.append('\n').append(getString(R.string.trend_no_goal));
        } else {
            text.append('\n').append(summary.getGoalProgressPercent() == null
                    ? getString(R.string.trend_goal_at_start)
                    : getString(R.string.trend_goal_progress, summary.getGoalProgressPercent()));
            text.append('\n').append(getString(R.string.trend_goal_distance,
                    unit.fromPounds(summary.getGoalDistancePounds()), unit.getSuffix()));
        }
        textTrendSummary.setText(text.toString());
    }
}
