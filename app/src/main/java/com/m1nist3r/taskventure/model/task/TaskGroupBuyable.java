package com.m1nist3r.taskventure.model.task;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class TaskGroupBuyable implements Parcelable {
    public static final Creator<TaskGroupBuyable> CREATOR = new Creator<TaskGroupBuyable>() {
        @Override
        public TaskGroupBuyable createFromParcel(Parcel in) {
            return new TaskGroupBuyable(in);
        }

        @Override
        public TaskGroupBuyable[] newArray(int size) {
            return new TaskGroupBuyable[size];
        }
    };
    private String Uid;
    private String name;
    private String imagePath;
    private ArrayList<Task> taskList;
    private String description;
    private String price;

    public TaskGroupBuyable() {}


    public TaskGroupBuyable(String uid, String name, String imagePath, ArrayList<Task> taskList, String description, String price) {
        Uid = uid;
        this.name = name;
        this.imagePath = imagePath;
        this.taskList = taskList;
        this.description = description;
        this.price = price;
    }

    protected TaskGroupBuyable(Parcel in) {
        Uid = in.readString();
        name = in.readString();
        imagePath = in.readString();
        description = in.readString();
        price = in.readString();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
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
        dest.writeString(price);
        dest.writeString(description);
        dest.writeTypedList(taskList);
    }
}
