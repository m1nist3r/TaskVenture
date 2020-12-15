package com.m1nist3r.taskventure.model.task;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class TaskGroup implements Parcelable {
    public static final Creator<TaskGroup> CREATOR = new Creator<TaskGroup>() {
        @Override
        public TaskGroup createFromParcel(Parcel in) {
            return new TaskGroup(in);
        }

        @Override
        public TaskGroup[] newArray(int size) {
            return new TaskGroup[size];
        }
    };
    private String Uid;
    private String name;
    private String imagePath;
    private ArrayList<Task> taskList;

    public TaskGroup() {
    }

    public TaskGroup(String uid, String name, String imagePath, ArrayList<Task> taskList) {
        this.Uid = uid;
        this.name = name;
        this.taskList = taskList;
        this.imagePath = imagePath;
    }


    protected TaskGroup(Parcel in) {
        Uid = in.readString();
        name = in.readString();
        imagePath = in.readString();
        taskList = new ArrayList<>();
        in.readTypedList(taskList, Task.CREATOR);
    }

    public String getId() {
        return Uid;
    }

    public void setId(String Uid) {
        this.Uid = Uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ArrayList<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Uid);
        dest.writeString(name);
        dest.writeString(imagePath);
        dest.writeTypedList(taskList);
    }
}
