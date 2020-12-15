package com.m1nist3r.taskventure.model.task;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.m1nist3r.taskventure.db.DriverManager;

public class TaskGroupBuyableServiceFirebaseImpl implements ITaskGroupBuyableService {

    private static final String TAG = "TaskGroupBuyableServiceFirebase";
    private static final String PREMIUM_SETS = "premium_sets";

    private final FirebaseFirestore db;

    public TaskGroupBuyableServiceFirebaseImpl() {
        this.db = DriverManager.getConnection();
    }

    @Override
    public Query findAllTaskGroupBuyable() {
        return db
                .collection(PREMIUM_SETS);
    }

    @Override
    public void saveTask(TaskGroupBuyable taskGroupBuyable) {
        db
                .collection(PREMIUM_SETS)
                .document(taskGroupBuyable.getId())
                .set(taskGroupBuyable)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Task Group was written with ID: "
                                + taskGroupBuyable.getId()))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error saving to player task group", e));
    }
}
