package com.m1nist3r.taskventure.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.PlayerAdapter;
import com.m1nist3r.taskventure.databinding.ActivityCreateGameBinding;
import com.m1nist3r.taskventure.model.player.Player;
import com.m1nist3r.taskventure.model.task.TaskGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateGameActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_TASK_GROUP_CODE = 2244;
    private static final String TAG = "CREATE_GAME ";
    private FirebaseAuth mAuth;
    private RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> mAdapter;
    private ArrayList<Player> playerList;
    private TaskGroup taskGroup;
    private ActivityCreateGameBinding mBinding;

    public static List<Player> getDuplicates(List<Player> players,
                                             Function<Player, String> classifier) {
        Map<String, List<Player>> map = players.stream()
                .collect(Collectors.groupingBy(classifier,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        return map.values().stream()
                .filter(personList -> personList.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mBinding = ActivityCreateGameBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        RecyclerView recyclerView = mBinding.recyclerViewAddPlayer;
        recyclerView.setHasFixedSize(true);

        FloatingActionButton fab = mBinding.floatingActionButtonPlayer;
        fab.setOnClickListener(view -> {
            if (mAdapter.getItemCount() > 5) {
                Toast.makeText(getApplicationContext(), "Maximum 5 players", Toast.LENGTH_LONG).show();
                return;
            }
            playerList.add(new Player("", 0, 0));
            mAdapter.notifyItemInserted(mAdapter.getItemCount() + 1);
            Log.d(TAG, "Player added");
        });

        playerList = new ArrayList<>();
        playerList.addAll(Arrays.asList(new Player("", 0, 0),
                new Player("", 0, 0)));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new PlayerAdapter(getApplicationContext(), playerList);
        recyclerView.setAdapter(mAdapter);

        mBinding.startCreateGame.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start_create_game) {
            if (taskGroup == null) {
                startActivityForResult(new Intent(this,
                        SelectGroupTaskActivity.class), REQUEST_TASK_GROUP_CODE);
            } else if (validateName(playerList) && taskGroup != null) {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putParcelableArrayListExtra("playerList", playerList);
                intent.putExtra("taskGroup", taskGroup);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TASK_GROUP_CODE && data != null) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    taskGroup = extras.getParcelable("taskGroup");
                    mBinding.startCreateGame.setText(R.string.start_game);
                }
            }
        }
    }

    private boolean validateName(List<Player> playerList) {
        if (getDuplicates(playerList, Player::getName).size() > 1) {
            Toast.makeText(this, "Player names should be unique", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Player names should be unique");
            return false;
        } else if (playerList.stream().anyMatch(player -> player.getName().length() < 4)) {
            Toast.makeText(this, "Player name should contain more than 4 letter", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Player name should contain more than 4 letter");
            return false;
        } else {
            return true;
        }
    }
}
