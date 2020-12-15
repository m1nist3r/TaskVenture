package com.m1nist3r.taskventure.model.task;


import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {
    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    private String Uid;
    private String description;
    private long time;
    private long period;

    public Task() {
    }

    public Task(String uid, String description, long time, long period) {
        this.Uid = uid;
        this.description = description;
        this.time = time;
        this.period = period;
    }

    protected Task(Parcel in) {
        Uid = in.readString();
        description = in.readString();
        time = in.readLong();
        period = in.readLong();
    }

    public String getId() {
        return Uid;
    }

    public void setId(String Uid) {
        this.Uid = Uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Uid);
        dest.writeString(description);
        dest.writeLong(time);
        dest.writeLong(period);
    }
}
