package com.example.firebasepw;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseHelper {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public Task<Void> addService(Service service) {
        String id = db.collection("services").document().getId();
        service.setId(id);
        return db.collection("services")
                .document(id)
                .set(service);
    }

    public Task<Void> updateService(Service service) {
        return db.collection("services")
                .document(service.getId())
                .set(service);
    }

    public Task<Void> deleteService(String serviceId) {
        return db.collection("services")
                .document(serviceId)
                .delete();
    }

    public Task<QuerySnapshot> getAllServices() {
        return db.collection("services").get();
    }

    public void logAction(String action) {
        if (isAdmin()) {
            LogEntry entry = new LogEntry(
                    db.collection("logs").document().getId(),
                    auth.getCurrentUser().getUid(),
                    action,
                    System.currentTimeMillis()
            );
            db.collection("logs").document(entry.getId()).set(entry);
        }
    }

    private boolean isAdmin() {
        // Здесь должна быть проверка роли, например:
        return true; // Упрощенная версия, в реальном приложении нужно проверять роль
    }
}