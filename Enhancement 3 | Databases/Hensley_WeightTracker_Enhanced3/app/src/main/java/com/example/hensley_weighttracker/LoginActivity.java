package com.example.hensley_weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.hensley_weighttracker.service.AuthService;

public class LoginActivity extends DatabaseActivity {
    private EditText editUsername;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private AuthService authService;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        authService = new AuthService(this);
        buttonLogin.setOnClickListener(view -> loginUser());
        buttonCreateAccount.setOnClickListener(view -> createAccount());
    }

    private void loginUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        databaseTask(() -> authService.login(username, password), result -> {
            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_ID", result.getValue());
            editPassword.setText("");
            startActivity(intent);
        }, buttonLogin, buttonCreateAccount, editUsername, editPassword);
    }

    private void createAccount() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        databaseTask(() -> authService.createAccount(username, password), result -> {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            if (result.isSuccess()) editPassword.setText("");
        }, buttonLogin, buttonCreateAccount, editUsername, editPassword);
    }
}
