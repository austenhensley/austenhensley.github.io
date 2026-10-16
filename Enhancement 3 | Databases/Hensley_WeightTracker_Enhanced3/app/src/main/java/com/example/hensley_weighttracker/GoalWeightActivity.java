package com.example.hensley_weighttracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.hensley_weighttracker.service.WeightService;
import java.util.Locale;

public class GoalWeightActivity extends DatabaseActivity {
    private EditText editGoalWeight;
    private Button buttonSaveGoal;
    private WeightService weightService;
    private int userId;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_goal_weight);
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) { finish(); return; }
        weightService = new WeightService(this);
        editGoalWeight = findViewById(R.id.editGoalWeight);
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);
        Button cancel = findViewById(R.id.buttonCancelGoal);
        databaseTask(() -> {
            String suffix = weightService.getPreferredUnit(userId).getSuffix();
            Double goal = weightService.getGoalForDisplay(userId);
            return new GoalData(suffix, goal);
        }, data -> {
            editGoalWeight.setHint(getString(R.string.goal_weight_hint_with_unit, data.suffix));
            if (data.goal != null) editGoalWeight.setText(String.format(Locale.US, "%.1f", data.goal));
        }, buttonSaveGoal, editGoalWeight);
        buttonSaveGoal.setOnClickListener(v -> saveGoalWeight());
        cancel.setOnClickListener(v -> finish());
    }

    private void saveGoalWeight() {
        String text = editGoalWeight.getText().toString();
        databaseTask(() -> weightService.saveGoal(userId, text), result -> {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            if (result.isSuccess()) finish();
        }, buttonSaveGoal, editGoalWeight);
    }

    private static class GoalData {
        final String suffix;
        final Double goal;
        GoalData(String suffix, Double goal) { this.suffix = suffix; this.goal = goal; }
    }
}
