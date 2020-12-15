package com.m1nist3r.taskventure.model.task;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m1nist3r.taskventure.db.DriverManager;

import java.util.Objects;

public class TaskServiceFirebaseImpl implements ITaskService {

    private static final String TAG = "TaskServiceFirebase";

    private static final String PLAYER = "player";
    private static final String GROUP_TASK = "group_task";
    private static final String TASK = "task";


    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public TaskServiceFirebaseImpl() {
        this.db = DriverManager.getConnection();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public com.google.android.gms.tasks.Task<DocumentSnapshot> findAllTasks(TaskGroup taskGroup) {
        return db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK)
                .document(taskGroup.getId())
                .get();
    }

    @Override
    public void saveTask(TaskGroup taskGroup, Task task) {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK)
                .document(taskGroup.getId())
                .update("taskList", FieldValue.arrayUnion(task))
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Task was written with ID: "
                                + task.getId()))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error saving to group_task", e));
    }

    @Override
    public void deleteTask(TaskGroup taskGroup, Task task) {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GROUP_TASK)
                .document(taskGroup.getId())
                .update("taskList", FieldValue.arrayRemove(task))
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Task was deleted"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error deleting task from group_task", e));
    }
}
