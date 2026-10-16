package com.example.hensley_weighttracker;

import android.app.DatePickerDialog;
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
import com.example.hensley_weighttracker.model.DisplayWeightEntry;
import com.example.hensley_weighttracker.model.WeightTrendSummary;
import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.SettingsService;
import com.example.hensley_weighttracker.service.WeightService;
import com.example.hensley_weighttracker.util.DateCodec;
import com.example.hensley_weighttracker.util.WeightUnit;
import java.time.LocalDate;
import java.util.List;

public class DashboardActivity extends DatabaseActivity {
    private int userId;
    private WeightService weightService;
    private SettingsService settingsService;
    private TextView textGoalValue, textWeightHeader, textTrendSummary, textHistoryFilter;
    private TableLayout tableWeightHistory;
    private Spinner spinnerWeightUnit;
    private Button buttonFilterStart, buttonFilterEnd;
    private WeightUnit displayedUnit = WeightUnit.LB;
    private boolean unitReady;
    private String startDate, endDate, pendingStart, pendingEnd;
    private int refreshVersion;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_dashboard);
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) { finish(); return; }
        weightService = new WeightService(this);
        settingsService = new SettingsService(this);
        textGoalValue = findViewById(R.id.textGoalValue);
        textWeightHeader = findViewById(R.id.textWeightHeader);
        textTrendSummary = findViewById(R.id.textTrendSummary);
        textHistoryFilter = findViewById(R.id.textHistoryFilter);
        tableWeightHistory = findViewById(R.id.tableWeightHistory);
        spinnerWeightUnit = findViewById(R.id.spinnerWeightUnit);
        buttonFilterStart = findViewById(R.id.buttonFilterStart);
        buttonFilterEnd = findViewById(R.id.buttonFilterEnd);
        if (state != null) {
            startDate = state.getString("FILTER_START");
            endDate = state.getString("FILTER_END");
            pendingStart = state.getString("PENDING_START");
            pendingEnd = state.getString("PENDING_END");
        }
        configureUnitSpinner();
        findViewById(R.id.buttonAddWeight).setOnClickListener(v -> openActivity(AddWeightActivity.class));
        findViewById(R.id.buttonSetGoal).setOnClickListener(v -> openActivity(GoalWeightActivity.class));
        findViewById(R.id.buttonNotifications).setOnClickListener(v -> openActivity(NotificationActivity.class));
        buttonFilterStart.setOnClickListener(v -> chooseDate(true));
        buttonFilterEnd.setOnClickListener(v -> chooseDate(false));
        findViewById(R.id.buttonApplyFilter).setOnClickListener(v -> applyFilter());
        findViewById(R.id.buttonClearFilter).setOnClickListener(v -> {
            startDate = null; endDate = null; pendingStart = null; pendingEnd = null;
            updateFilterLabels(); refreshDashboard();
        });
        updateFilterLabels();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("FILTER_START", startDate);
        state.putString("FILTER_END", endDate);
        state.putString("PENDING_START", pendingStart);
        state.putString("PENDING_END", pendingEnd);
    }

    @Override protected void onResume() {
        super.onResume();
        if (weightService != null) refreshDashboard();
    }

    @Override protected void onPause() {
        super.onPause();
        refreshVersion++;
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void configureUnitSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weight_unit_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeightUnit.setAdapter(adapter);
        spinnerWeightUnit.setEnabled(false);
        spinnerWeightUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WeightUnit selected = position == 1 ? WeightUnit.KG : WeightUnit.LB;
                if (!unitReady || selected == displayedUnit) return;
                unitReady = false;
                databaseTask(() -> settingsService.saveWeightUnit(userId, selected), saved -> {
                    if (!saved) Toast.makeText(DashboardActivity.this,
                            R.string.unable_to_save_unit, Toast.LENGTH_SHORT).show();
                    refreshDashboard();
                }, () -> refreshDashboard(), spinnerWeightUnit);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void chooseDate(boolean start) {
        String selected = start ? pendingStart : pendingEnd;
        LocalDate initial = selected == null ? LocalDate.now() : LocalDate.parse(selected);
        new DatePickerDialog(this, (picker, year, month, day) -> {
            String date = LocalDate.of(year, month + 1, day).toString();
            if (start) pendingStart = date; else pendingEnd = date;
            updateFilterLabels();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void updateFilterLabels() {
        buttonFilterStart.setText(pendingStart == null ? getString(R.string.filter_start)
                : getString(R.string.filter_start_value, DateCodec.toDisplay(pendingStart)));
        buttonFilterEnd.setText(pendingEnd == null ? getString(R.string.filter_end)
                : getString(R.string.filter_end_value, DateCodec.toDisplay(pendingEnd)));
    }

    private void applyFilter() {
        if (pendingStart == null || pendingEnd == null || pendingStart.compareTo(pendingEnd) > 0) {
            Toast.makeText(this, R.string.filter_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        startDate = pendingStart;
        endDate = pendingEnd;
        refreshDashboard();
    }

    private void refreshDashboard() {
        final int request = ++refreshVersion;
        final String from = startDate, through = endDate;
        final boolean filtered = from != null && through != null && from.compareTo(through) <= 0;
        unitReady = false;
        databaseTask(() -> {
            DashboardData data = new DashboardData();
            data.unit = weightService.getPreferredUnit(userId);
            data.goal = weightService.getGoalForDisplay(userId);
            data.trend = weightService.getTrendSummary(userId);
            data.entries = filtered ? weightService.getWeightHistoryBetween(userId, from, through)
                    : weightService.getWeightHistory(userId);
            return data;
        }, data -> {
            if (request != refreshVersion) return;
            displayedUnit = data.unit;
            spinnerWeightUnit.setSelection(data.unit == WeightUnit.KG ? 1 : 0);
            unitReady = true;
            textGoalValue.setText(data.goal == null ? getString(R.string.no_goal_set)
                    : getString(R.string.goal_weight_display, data.goal, data.unit.getSuffix()));
            renderTrend(data.trend, data.unit);
            textHistoryFilter.setText(filtered
                    ? getString(R.string.filter_active, DateCodec.toDisplay(from), DateCodec.toDisplay(through))
                    : getString(R.string.filter_all));
            renderHistory(data.entries, data.unit);
        }, spinnerWeightUnit);
    }

    private void renderHistory(List<DisplayWeightEntry> entries, WeightUnit unit) {
        while (tableWeightHistory.getChildCount() > 1) tableWeightHistory.removeViewAt(1);
        textWeightHeader.setText(getString(R.string.weight_header_with_unit, unit.getSuffix()));
        if (entries.isEmpty()) {
            TableRow row = new TableRow(this);
            TextView text = new TextView(this);
            text.setText(R.string.no_matching_entries);
            text.setPadding(16,16,16,16);
            row.addView(text);
            tableWeightHistory.addView(row);
            return;
        }
        for (DisplayWeightEntry entry : entries) {
            TableRow row = new TableRow(this);
            TextView date = new TextView(this);
            date.setText(entry.getDate()); date.setPadding(8,8,8,8);
            TextView weight = new TextView(this);
            weight.setText(getString(R.string.weight_value_display, entry.getDisplayWeight(), unit.getSuffix()));
            weight.setPadding(8,8,8,8);
            Button edit = new Button(this); edit.setText(R.string.edit_button_text);
            edit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddWeightActivity.class);
                intent.putExtra("USER_ID", userId); intent.putExtra("WEIGHT_ID", entry.getId());
                startActivity(intent);
            });
            Button delete = new Button(this); delete.setText(R.string.delete_button_text);
            delete.setOnClickListener(v -> databaseTask(() -> weightService.deleteWeight(entry.getId(), userId),
                    deleted -> {
                        Toast.makeText(this, deleted ? R.string.weight_entry_deleted : R.string.unable_to_delete_entry,
                                Toast.LENGTH_SHORT).show();
                        refreshDashboard();
                    }, edit, delete));
            row.addView(date); row.addView(weight); row.addView(edit); row.addView(delete);
            tableWeightHistory.addView(row);
        }
    }

    private void renderTrend(ServiceResult<WeightTrendSummary> result, WeightUnit unit) {
        if (!result.isSuccess()) {
            textTrendSummary.setText(getString(R.string.trend_unavailable, result.getMessage()));
            return;
        }
        WeightTrendSummary summary = result.getValue();
        if (summary.getEntryCount() == 0) {
            textTrendSummary.setText(R.string.trend_no_data);
            return;
        }

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

    private static class DashboardData {
        WeightUnit unit;
        Double goal;
        ServiceResult<WeightTrendSummary> trend;
        List<DisplayWeightEntry> entries;
    }
}
