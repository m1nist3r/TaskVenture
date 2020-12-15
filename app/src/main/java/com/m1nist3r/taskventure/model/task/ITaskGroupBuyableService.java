package com.m1nist3r.taskventure.model.task;

import com.google.firebase.firestore.Query;

public interface ITaskGroupBuyableService {
    Query findAllTaskGroupBuyable();

    void saveTask(TaskGroupBuyable taskGroupBuyable);
}
