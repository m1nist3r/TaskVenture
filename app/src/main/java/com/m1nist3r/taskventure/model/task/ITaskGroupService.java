package com.m1nist3r.taskventure.model.task;

import com.google.firebase.firestore.Query;

public interface ITaskGroupService {
    Query findAllTaskGroups();

    void saveTask(TaskGroup taskGroup);

    void deleteTask(TaskGroup taskGroup);
}
