package com.example.firebasepw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private Button addUserButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private UsersAdapter adapter;
    private List<User> userList;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = FirebaseFirestore.getInstance();
        authManager = new AuthManager();
        userList = new ArrayList<>();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        addUserButton = findViewById(R.id.addUserButton);
        progressBar = findViewById(R.id.progressBar);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(userList, this::showEditUserDialog, this::deleteUser);
        usersRecyclerView.setAdapter(adapter);

        addUserButton.setOnClickListener(v -> showAddUserDialog());

        loadUsers();
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_user_register, null);

        EditText emailEditText = view.findViewById(R.id.emailEditText);
        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText roleEditText = view.findViewById(R.id.roleEditText);
        EditText passwordEditText = view.findViewById(R.id.passwordEditText);

        builder.setView(view)
                .setTitle("Добавить пользователя")
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String email = emailEditText.getText().toString().trim();
                    String name = nameEditText.getText().toString().trim();
                    String role = roleEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    if (validateInput(email, name, role, password)) {
                        addUser(email, name, role, password);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_user_edit, null);

        EditText emailEditText = view.findViewById(R.id.emailEditText);
        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText roleEditText = view.findViewById(R.id.roleEditText);

        emailEditText.setText(user.getEmail());
        nameEditText.setText(user.getName());
        roleEditText.setText(user.getRole());

        builder.setView(view)
                .setTitle("Редактировать пользователя")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String email = emailEditText.getText().toString().trim();
                    String name = nameEditText.getText().toString().trim();
                    String role = roleEditText.getText().toString().trim();

                    if (validateEditInput(email, name, role)) {
                        updateUser(user.getId(), email, name, role);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private boolean validateInput(String email, String name, String role, String password) {
        if (email.isEmpty() || name.isEmpty() || role.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!role.equals("admin") && !role.equals("employee")) {
            Toast.makeText(this, "Роль должна быть 'admin' или 'employee'", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateEditInput(String email, String name, String role) {
        if (email.isEmpty() || name.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!role.equals("admin") && !role.equals("employee")) {
            Toast.makeText(this, "Роль должна быть 'admin' или 'employee'", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addUser(String email, String name, String role, String password) {
        progressBar.setVisibility(View.VISIBLE);
        authManager.register(email, password, name, role, new AuthManager.RegisterCallback() {
            @Override
            public void onSuccess(String userId) {
                User user = new User(userId, email, role, name);
                userList.add(user);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                logAction("Добавлен пользователь: " + email);
                Toast.makeText(UserManagementActivity.this,
                        "Пользователь успешно зарегистрирован", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this,
                        "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUser(String userId, String email, String name, String role) {
        progressBar.setVisibility(View.VISIBLE);
        User updatedUser = new User(userId, email, role, name);

        db.collection("users")
                .document(userId)
                .set(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    int index = -1;
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getId().equals(userId)) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        userList.set(index, updatedUser);
                        adapter.notifyItemChanged(index);
                    }
                    progressBar.setVisibility(View.GONE);
                    logAction("Изменен пользователь: " + email);
                    Toast.makeText(this, "Пользователь обновлен", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить пользователя")
                .setMessage("Вы уверены, что хотите удалить " + user.getEmail() + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("users")
                            .document(user.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                userList.remove(user);
                                adapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                logAction("Удален пользователь: " + user.getEmail());
                                Toast.makeText(this, "Пользователь удален", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void logAction(String action) {
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LogEntry entry = new LogEntry("", adminId, action, System.currentTimeMillis());
        db.collection("logs").add(entry);
    }
}