package com.example.firebasepw;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthManager {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AuthCallback {
        void onSuccess(String role);
        void onError(Exception e);
    }

    public interface RegisterCallback {
        void onSuccess(String userId);
        void onError(Exception e);
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    checkUserRole(authResult.getUser(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    public void register(String email, String password, String name, String role, RegisterCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        User newUser = new User(user.getUid(), email, role, name);
                        db.collection("users")
                                .document(user.getUid())
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    user.getIdToken(true).addOnSuccessListener(result -> {
                                        callback.onSuccess(user.getUid());
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    user.delete();
                                    callback.onError(e);
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    private void checkUserRole(FirebaseUser user, AuthCallback callback) {
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        callback.onSuccess(role);
                    } else {
                        callback.onError(new Exception("Пользователь не найден в базе данных"));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }
}