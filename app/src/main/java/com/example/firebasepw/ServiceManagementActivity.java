package com.example.firebasepw;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagementActivity extends AppCompatActivity {
    private RecyclerView servicesRecyclerView;
    private ServicesAdapter adapter;
    private FirebaseHelper firebaseHelper;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private List<Service> serviceList;
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_management);

        firebaseHelper = new FirebaseHelper();
        serviceList = new ArrayList<>();
        categories = new ArrayList<>();
        categories.add("Все категории"); // Добавляем опцию для отображения всех услуг

        // Инициализация UI
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        Button addServiceButton = findViewById(R.id.addServiceButton);

        setupRecyclerView();
        setupSearch();
        setupCategoryFilter();

        addServiceButton.setOnClickListener(v -> addNewService());
        checkUserRole();
        loadServices();
    }

    private void setupRecyclerView() {
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServicesAdapter(serviceList,
                this::showEditServiceDialog,
                this::deleteService);
        servicesRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterServices(s.toString());
            }
        });
    }

    private void setupCategoryFilter() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories.get(position);
                filterServices(searchEditText.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadServices() {
        firebaseHelper.getAllServices()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceList.clear();
                    categories.clear();
                    categories.add("Все категории");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Service service = doc.toObject(Service.class);
                        serviceList.add(service);
                        if (!categories.contains(service.getCategory())) {
                            categories.add(service.getCategory());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    ((ArrayAdapter) categorySpinner.getAdapter()).notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки услуг: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void filterServices(String query) {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        List<Service> filteredList = new ArrayList<>();

        for (Service service : serviceList) {
            boolean matchesSearch = query.isEmpty() ||
                    service.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesCategory = selectedCategory.equals("Все категории") ||
                    service.getCategory().equals(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredList.add(service);
            }
        }

        adapter.updateList(filteredList);
    }

    private void checkUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
    }

    private void addNewService() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service, null);

        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText categoryEditText = view.findViewById(R.id.categoryEditText);
        EditText descriptionEditText = view.findViewById(R.id.descriptionEditText);
        EditText priceEditText = view.findViewById(R.id.priceEditText);

        builder.setView(view)
                .setTitle("Добавить услугу")
                .setPositiveButton("Добавить", (dialog, which) -> {
                    try {
                        String name = nameEditText.getText().toString().trim();
                        String category = categoryEditText.getText().toString().trim();
                        String description = descriptionEditText.getText().toString().trim();
                        double price = Double.parseDouble(priceEditText.getText().toString().trim());

                        if (name.isEmpty() || category.isEmpty()) {
                            Toast.makeText(this, "Название и категория обязательны",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Service service = new Service("", name, category, description, price);
                        firebaseHelper.addService(service)
                                .addOnSuccessListener(aVoid -> {
                                    firebaseHelper.logAction("Добавлена услуга: " + name);
                                    loadServices();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Неверный формат цены", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditServiceDialog(Service service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_service, null);

        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText categoryEditText = view.findViewById(R.id.categoryEditText);
        EditText descriptionEditText = view.findViewById(R.id.descriptionEditText);
        EditText priceEditText = view.findViewById(R.id.priceEditText);

        nameEditText.setText(service.getName());
        categoryEditText.setText(service.getCategory());
        descriptionEditText.setText(service.getDescription());
        priceEditText.setText(String.valueOf(service.getPrice()));

        builder.setView(view)
                .setTitle("Редактировать услугу")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        String name = nameEditText.getText().toString().trim();
                        String category = categoryEditText.getText().toString().trim();
                        String description = descriptionEditText.getText().toString().trim();
                        double price = Double.parseDouble(priceEditText.getText().toString().trim());

                        if (name.isEmpty() || category.isEmpty()) {
                            Toast.makeText(this, "Название и категория обязательны",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Service updatedService = new Service(service.getId(), name, category,
                                description, price);
                        firebaseHelper.updateService(updatedService)
                                .addOnSuccessListener(aVoid -> {
                                    firebaseHelper.logAction("Изменена услуга: " + name);
                                    loadServices();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Неверный формат цены", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteService(Service service) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить услугу")
                .setMessage("Вы уверены, что хотите удалить " + service.getName() + "?")
                .setPositiveButton("Да", (dialog, which) -> {
                    firebaseHelper.deleteService(service.getId())
                            .addOnSuccessListener(aVoid -> {
                                firebaseHelper.logAction("Удалена услуга: " + service.getName());
                                loadServices();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}