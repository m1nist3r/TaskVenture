package com.m1nist3r.taskventure.model.game;

import com.m1nist3r.taskventure.model.player.Player;
import com.m1nist3r.taskventure.model.task.TaskGroup;

import java.util.ArrayList;

public class Game {
    private ArrayList<Player> players;
    private TaskGroup taskGroup;
    private int playerPosition;
    private int taskPosition;

    public Game() {
    }

    public Game(ArrayList<Player> players, TaskGroup taskGroup, int playerPosition, int taskPosition) {
        this.players = players;
        this.taskGroup = taskGroup;
        this.playerPosition = playerPosition;
        this.taskPosition = taskPosition;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }

    public int getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(int playerPosition) {
        this.playerPosition = playerPosition;
    }

    public int getTaskPosition() {
        return taskPosition;
    }

    public void setTaskPosition(int taskPosition) {
        this.taskPosition = taskPosition;
    }
}
