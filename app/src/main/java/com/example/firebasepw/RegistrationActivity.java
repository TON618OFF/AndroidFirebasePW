package com.example.firebasepw;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText, roleEditText;
    private Button registerButton;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        authManager = new AuthManager();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        roleEditText = findViewById(R.id.roleEditText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String role = roleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Введите email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Введите пароль");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Пароль должен быть не менее 6 символов");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Введите имя");
            return;
        }
        if (!role.equals("admin") && !role.equals("employee")) {
            roleEditText.setError("Роль должна быть 'admin' или 'employee'");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        authManager.register(email, password, name, role, new AuthManager.RegisterCallback() {
            @Override
            public void onSuccess(String userId) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegistrationActivity.this,
                        "Регистрация успешна", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegistrationActivity.this,
                        "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}