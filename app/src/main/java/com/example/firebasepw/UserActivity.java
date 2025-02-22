package com.example.firebasepw;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    private RecyclerView servicesRecyclerView;
    private ServicesAdapter servicesAdapter;
    private TextView selectedDateTimeTextView;
    private Button selectDateTimeButton, confirmAppointmentButton, logoutButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Service> serviceList;
    private Service selectedService;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        serviceList = new ArrayList<>();
        selectedDateTime = Calendar.getInstance();

        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        selectDateTimeButton = findViewById(R.id.selectDateTimeButton);
        confirmAppointmentButton = findViewById(R.id.confirmAppointmentButton);
        logoutButton = findViewById(R.id.logoutButton);

        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        servicesAdapter = new ServicesAdapter(serviceList, this::onServiceSelected, false);
        servicesRecyclerView.setAdapter(servicesAdapter);

        loadServices();

        selectDateTimeButton.setOnClickListener(v -> showDatePickerDialog());
        confirmAppointmentButton.setOnClickListener(v -> createAppointment());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void loadServices() {
        db.collection("services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Список услуг пуст", Toast.LENGTH_LONG).show();
                    } else {
                        for (Service service : queryDocumentSnapshots.toObjects(Service.class)) {
                            serviceList.add(service);
                        }
                        Toast.makeText(this, "Загружено услуг: " + serviceList.size(), Toast.LENGTH_SHORT).show();
                        servicesAdapter.updateList(serviceList); // Обновляем адаптер
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки услуг: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void onServiceSelected(Service service) {
        selectedService = service;
        Toast.makeText(this, "Выбрана услуга: " + service.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDateTime.set(selectedYear, selectedMonth, selectedDay);
                    showTimePickerDialog();
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDateTime.set(Calendar.MINUTE, selectedMinute);
                    updateDateTimeDisplay();
                },
                hour, minute,
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        selectedDateTimeTextView.setText("Выбрано: " + dateFormat.format(selectedDateTime.getTime()));
    }

    private void createAppointment() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedService == null) {
            Toast.makeText(this, "Выберите услугу", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateTimeTextView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Выберите дату и время", Toast.LENGTH_SHORT).show();
            return;
        }

        String clientId = mAuth.getCurrentUser().getUid();
        String clientName = mAuth.getCurrentUser().getEmail();
        String serviceId = selectedService.getId();
        String serviceName = selectedService.getName();
        String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDateTime.getTime());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedDateTime.getTime());

        Appointment appointment = new Appointment(clientId, clientName, serviceId, serviceName, date, time);

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Запись успешно создана", Toast.LENGTH_SHORT).show();
                    resetSelection();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при создании записи: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void resetSelection() {
        selectedService = null;
        selectedDateTime = Calendar.getInstance();
        selectedDateTimeTextView.setText("");
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}