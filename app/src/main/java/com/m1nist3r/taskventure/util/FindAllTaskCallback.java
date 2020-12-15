package com.m1nist3r.taskventure.util;

import com.m1nist3r.taskventure.model.task.Task;

import java.util.ArrayList;

public interface FindAllTaskCallback {
    void onCallback(ArrayList<Task> tasks);
}
