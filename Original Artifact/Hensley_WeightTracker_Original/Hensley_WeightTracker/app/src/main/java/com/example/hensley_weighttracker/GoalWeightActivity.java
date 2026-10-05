package com.example.hensley_weighttracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GoalWeightActivity extends AppCompatActivity {

    private EditText editGoalWeight;
    private Button buttonSaveGoal;
    private Button buttonCancelGoal;
    private DBHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_weight);

        dbHelper = new DBHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);

        editGoalWeight = findViewById(R.id.editGoalWeight);
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);
        buttonCancelGoal = findViewById(R.id.buttonCancelGoal);

        loadCurrentGoal();

        buttonSaveGoal.setOnClickListener(view -> saveGoalWeight());
        buttonCancelGoal.setOnClickListener(view -> finish());
    }

    private void loadCurrentGoal() {
        Double goalWeight = dbHelper.getGoalWeight(userId);

        if (goalWeight != null) {
            editGoalWeight.setText(String.valueOf(goalWeight));
        }
    }

    private void saveGoalWeight() {
        String goalText = editGoalWeight.getText().toString().trim();

        if (goalText.isEmpty()) {
            Toast.makeText(this, "Enter a goal weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        double goalWeight = Double.parseDouble(goalText);
        dbHelper.saveGoalWeight(userId, goalWeight);

        Toast.makeText(this, "Goal weight saved.", Toast.LENGTH_SHORT).show();
        finish();
    }
}