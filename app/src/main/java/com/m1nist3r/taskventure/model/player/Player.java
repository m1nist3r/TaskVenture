package com.m1nist3r.taskventure.model.player;


import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
    private String name;
    private int score;
    private int incomplete;

    public Player() {
    }

    public Player(String name, int score, int incomplete) {
        this.name = name;
        this.score = score;
        this.incomplete = incomplete;
    }

    protected Player(Parcel in) {
        name = in.readString();
        score = in.readInt();
        incomplete = in.readInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(int incomplete) {
        this.incomplete = incomplete;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(score);
        dest.writeInt(incomplete);
    }
}
