package com.m1nist3r.taskventure.model.task;

import com.google.firebase.firestore.DocumentSnapshot;

public interface ITaskService {
    com.google.android.gms.tasks.Task<DocumentSnapshot> findAllTasks(TaskGroup taskGroup);

    void saveTask(TaskGroup taskGroup, Task task);

    void deleteTask(TaskGroup taskGroup, Task task);
}
