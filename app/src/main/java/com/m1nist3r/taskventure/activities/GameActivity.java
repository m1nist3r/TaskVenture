package com.m1nist3r.taskventure.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ParcelFormatException;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.databinding.ActivityGameBinding;
import com.m1nist3r.taskventure.model.game.Game;
import com.m1nist3r.taskventure.model.game.GameServiceFirebaseImpl;
import com.m1nist3r.taskventure.model.game.IGameService;
import com.m1nist3r.taskventure.model.player.Player;
import com.m1nist3r.taskventure.model.task.Task;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.util.CheckNetwork;
import com.m1nist3r.taskventure.util.GameCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;
import static java.lang.Math.random;

public class GameActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "GAME";
    private static final String TASK_FOR = "Task for ";
    private static final int ADD_CODE = 3898;
    private static final String CHANNEL_ID = "ID_1";

    private FirebaseAuth mAuth;
    private TaskGroup taskGroup;
    private ArrayList<Player> playerList;
    private IGameService gameService;
    private int taskPosition;
    private int playerPosition;
    private boolean isTimedStart;
    private ActivityGameBinding mBinding;
    private ArrayList<CountDownTimer> timers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();
        createNotificationChannel();

        mAuth = FirebaseAuth.getInstance();
        gameService = new GameServiceFirebaseImpl();

        mBinding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        Bundle bundle = getIntent().getExtras();

        taskGroup = null;
        playerList = null;
        taskPosition = 0;
        playerPosition = 0;
        isTimedStart = false;
        if (bundle == null) {
            readData(game -> {
                taskGroup = game.getTaskGroup();
                playerList = game.getPlayers();
                playerPosition = game.getPlayerPosition();
                taskPosition = game.getTaskPosition();
                initializeView();
            });
        } else {
            try {
                taskGroup = bundle.getParcelable("taskGroup");
                playerList = bundle.getParcelableArrayList("playerList");
                initializeView();
            } catch (ParcelFormatException e) {
                Log.w(TAG, "GAME: task group and player list parcel exception.\n"
                        + e.getMessage());
            }
        }
        mBinding.saveGameButton.setOnClickListener(this);
        mBinding.taskAcceptButton.setOnClickListener(this);
        mBinding.taskCancelButton.setOnClickListener(this);
        mBinding.endGameButton.setOnClickListener(this);
        mBinding.taskRefreshButton.setOnClickListener(this);
        mBinding.taskAddToCustomButton.setOnClickListener(this);

        timers = new ArrayList<>();
    }

    private Notification createNotification(String playerName, String taskDescription) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_white_24)
                .setContentTitle(playerName)
                .setContentText(taskDescription)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        return builder.build();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isNetworkConnected && mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
            }
        } else if (!isNetworkConnected) {
            AlertDialog dialog = displayMobileDataSettingsDialog(this);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        displayExitConfirmation();
    }

    public void displayExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Do you want to save your game?");
        builder.setPositiveButton("Save game", (dialog, which) -> saveGame());
        builder.setNegativeButton("Exit game", (dialog, which) -> endGame());

        AlertDialog exitDialog = builder.create();
        exitDialog.show();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Game Channel";
        String description = "Channel to notify players about their timed task";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void initializeView() {
        Collections.shuffle(playerList);
        Collections.shuffle(taskGroup.getTaskList());
        updateUI();
    }

    private void readData(GameCallback gameCallback) {
        gameService.loadGame()
                .addOnCompleteListener(documentSnapshot -> {
                    Game game = Objects.requireNonNull(documentSnapshot.getResult())
                            .toObject(Game.class);
                    gameCallback.onGameCallback(game);
                })
                .addOnFailureListener(e ->
                        Log.w(TAG, "GAME: game not loaded" + e.getMessage()));
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        Task task = taskGroup.getTaskList().get(taskPosition);
        Player playerC = playerList.get(playerPosition);

        mBinding.taskFor.setText(TASK_FOR + playerC.getName());
        if (task.getTime() == 0L) {
            mBinding.taskDescriptionGame
                    .setText(task.getDescription());
        } else if (task.getPeriod() == 0L) {
            mBinding.taskTimeGame.setText(getString(R.string.time_remain) + " " + task.getTime());
            mBinding.taskTimeGame.setVisibility(View.VISIBLE);
            mBinding.taskDescriptionGame
                    .setText(task.getDescription()
                            + "\n Time: " + task.getTime());
        } else {
            mBinding.taskDescriptionGame
                    .setText(task.getDescription()
                            + "\n Time: " + task.getTime()
                            + " Period: " + task.getPeriod());
        }
        mBinding.gameScoreResult.setText("");
        playerList.forEach(player -> mBinding.gameScoreResult.append(player.getName()
                + " score: " + player.getScore() + "\n"));
    }

    private void taskAccept() {
        Task task = taskGroup.getTaskList().get(taskPosition);
        Player playerC = playerList.get(playerPosition);

        if (task.getTime() == 0L) {
            updatePlayerAndTask(playerList.get(playerPosition).getScore() + 1);
        } else if (task.getPeriod() == 0L) {
            if (isTimedStart) {
                timers.get(timers.size() - 1).cancel();
                mBinding.taskTimeGame.setVisibility(View.GONE);
                updatePlayerAndTask(playerList.get(playerPosition).getScore() + 1);
                timers.remove(timers.size() - 1);
                isTimedStart = false;
            } else {
                timedTaskStart(playerC, task, 0L);
                isTimedStart = true;
            }
        } else {
            timedTaskStart(playerC, task, task.getPeriod());
            updatePlayerAndTask(playerC.getScore() + 1);
        }
    }

    private void taskCancel() {
        Task task = taskGroup.getTaskList().get(taskPosition);
        Player playerC = playerList.get(playerPosition);
        if (task.getTime() != 0L && task.getPeriod() == 0L) {
            if (isTimedStart) {
                timers.get(timers.size() - 1).cancel();
                mBinding.taskTimeGame.setVisibility(View.GONE);
                timers.remove(timers.size() - 1);
                isTimedStart = false;
            }
            mBinding.taskTimeGame.setVisibility(View.GONE);
        }
        updatePlayerAndTask(playerC.getScore() - 1);
    }

    private void taskRefresh() {
        taskPosition++;
        if (taskPosition == taskGroup.getTaskList().size()) {
            Collections.shuffle(taskGroup.getTaskList());
            taskPosition = 0;
        }
        updateUI();
    }

    private void timedTaskStart(Player player, Task task, long period) {
        final long interval;
        if (period == 0L) {
            interval = 1000L;
        } else {
            interval = period * 1000;
        }

        CountDownTimer timer = new CountDownTimer(task.getTime() * 1000, interval) {

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                if (interval == 1000L) {
                    mBinding.taskTimeGame.setText(getString(R.string.time_remain) + " " + millisUntilFinished / 1000);
                } else {
                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify((int) ((long) (random() * interval)),
                            createNotification(player.getName(), task.getDescription()));
                }
            }

            @Override
            public void onFinish() {
                if (interval == 1000L) {
                    updatePlayerAndTask(player.getScore() + 1);
                    mBinding.taskTimeGame.setVisibility(View.GONE);
                    isTimedStart = false;
                }
            }
        }.start();
        timers.add(timer);
    }


    private void taskAddToCustom() {
        Intent intent = new Intent(this, AddTaskToGroupActivity.class);
        intent.putExtra("task", taskGroup.getTaskList().get(taskPosition));
        startActivityForResult(intent, ADD_CODE);
    }

    private void updatePlayerAndTask(int score) {
        if (score < playerList.get(playerPosition).getScore())
            playerList.get(playerPosition).setIncomplete(playerList.get(playerPosition).getIncomplete() + 1);
        playerList.get(playerPosition).setScore(score);

        taskPosition++;
        playerPosition++;
        if (playerPosition == playerList.size()) {
            playerPosition = 0;
        }
        if (taskPosition == taskGroup.getTaskList().size()) {
            Collections.shuffle(taskGroup.getTaskList());
            taskPosition = 0;
        }
        updateUI();
    }

    private void endGame() {
        gameService.deleteGame();
        timers.forEach(CountDownTimer::cancel);
        Intent intent = new Intent(this, EndGameStatisticActivity.class);
        intent.putParcelableArrayListExtra("playerList", playerList);
        startActivity(intent);
        finish();
    }

    private void saveGame() {
        gameService.saveGame(new Game(playerList, taskGroup, playerPosition, taskPosition));
        timers.forEach(CountDownTimer::cancel);
        startActivity(new Intent(this, MainAppActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CODE && data != null) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: task saved");
            } else {
                Log.w(TAG, "onActivityResult: task not saved");
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == mBinding.saveGameButton.getId()) {
            saveGame();
        } else if (i == mBinding.endGameButton.getId()) {
            endGame();
        } else if (i == mBinding.taskAcceptButton.getId()) {
            taskAccept();
        } else if (i == mBinding.taskCancelButton.getId()) {
            taskCancel();
        } else if (i == mBinding.taskRefreshButton.getId()) {
            taskRefresh();
        } else if (i == mBinding.taskAddToCustomButton.getId()) {
            taskAddToCustom();
        }
    }
}
