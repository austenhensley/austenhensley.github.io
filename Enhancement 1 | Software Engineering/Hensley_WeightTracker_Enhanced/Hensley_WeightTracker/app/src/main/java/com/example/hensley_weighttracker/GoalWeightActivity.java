package com.example.hensley_weighttracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hensley_weighttracker.service.ServiceResult;
import com.example.hensley_weighttracker.service.WeightService;
import com.example.hensley_weighttracker.util.WeightUnit;

import java.util.Locale;

public class GoalWeightActivity extends AppCompatActivity {

    private EditText editGoalWeight;
    private WeightService weightService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, R.string.login_session_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        weightService = new WeightService(this);
        editGoalWeight = findViewById(R.id.editGoalWeight);
        Button buttonSaveGoal = findViewById(R.id.buttonSaveGoal);
        Button buttonCancelGoal = findViewById(R.id.buttonCancelGoal);

        WeightUnit unit = weightService.getPreferredUnit(userId);
        editGoalWeight.setHint(getString(R.string.goal_weight_hint_with_unit, unit.getSuffix()));

        loadCurrentGoal();
        buttonSaveGoal.setOnClickListener(view -> saveGoalWeight());
        buttonCancelGoal.setOnClickListener(view -> finish());
    }

    private void loadCurrentGoal() {
        Double goalWeight = weightService.getGoalForDisplay(userId);
        if (goalWeight != null) {
            editGoalWeight.setText(String.format(Locale.US, "%.1f", goalWeight));
        }
    }

    private void saveGoalWeight() {
        ServiceResult<Double> result = weightService.saveGoal(
                userId,
                editGoalWeight.getText().toString()
        );

        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        if (result.isSuccess()) {
            finish();
        }
    }
}
