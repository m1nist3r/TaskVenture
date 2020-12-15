package com.m1nist3r.taskventure.db;

import com.google.firebase.firestore.FirebaseFirestore;

public class DriverManager {
    public static FirebaseFirestore getConnection() {
        return FirebaseFirestore.getInstance();
    }
}
