package com.example.firebasepw;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authManager = new AuthManager();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_auth), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        checkCurrentUser();
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser);
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Введите email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Введите пароль");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String role) {
                redirectToDashboard(role);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(MainActivity.this,
                        "Ошибка входа: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserRole(FirebaseUser user) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        redirectToDashboard(role);
                    } else {
                        Toast.makeText(this,
                                "Пользователь не найден в базе данных",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        resetUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Ошибка проверки роли: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    resetUI();
                });
    }

    private void redirectToDashboard(String role) {
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("employee".equals(role)) {
            intent = new Intent(this, EmployeeDashboardActivity.class);
        } else {
            Toast.makeText(this, "Неизвестная роль пользователя", Toast.LENGTH_LONG).show();
            mAuth.signOut();
            resetUI();
            return;
        }

        progressBar.setVisibility(View.GONE);
        startActivity(intent);
        finish();
    }

    private void resetUI() {
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        emailEditText.setText("");
        passwordEditText.setText("");
    }
}