package com.example.hensley_weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hensley_weighttracker.service.AuthService;
import com.example.hensley_weighttracker.service.ServiceResult;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        authService = new AuthService(this);

        buttonLogin.setOnClickListener(view -> loginUser());
        buttonCreateAccount.setOnClickListener(view -> createAccount());
    }

    private void loginUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        ServiceResult<Integer> result = authService.login(username, password);

        if (!result.isSuccess()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("USER_ID", result.getValue());
        startActivity(intent);
    }

    private void createAccount() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        ServiceResult<Integer> result = authService.createAccount(username, password);

        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
        if (result.isSuccess()) {
            editPassword.setText("");
        }
    }
}
