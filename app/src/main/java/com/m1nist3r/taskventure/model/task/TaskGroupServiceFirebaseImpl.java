package com.m1nist3r.taskventure.model.task;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.m1nist3r.taskventure.db.DriverManager;

import java.util.Objects;

public class TaskGroupServiceFirebaseImpl implements ITaskGroupService {

    private static final String TAG = "TaskGroupServiceFirebase";

    private static final String PLAYER = "player";
    private static final String GROUP_TASK = "group_task";

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public TaskGroupServiceFirebaseImpl() {
        this.db = DriverManager.getConnection();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public Query findAllTaskGroups() {
        return db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK);
    }


    @Override
    public void saveTask(TaskGroup taskGroup) {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK)
                .document(taskGroup.getId())
                .set(taskGroup)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Task Group was written with ID: "
                                + taskGroup.getId()))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error saving to player task group", e));
    }

    @Override
    public void deleteTask(TaskGroup taskGroup) {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK)
                .document(taskGroup.getId())
                .delete()
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Task Group was deleted"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error deleting task group from player", e));
    }
}
