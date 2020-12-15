package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.SelectTaskGroupAdapter;
import com.m1nist3r.taskventure.databinding.SelectTaskGroupBinding;
import com.m1nist3r.taskventure.model.task.ITaskGroupService;
import com.m1nist3r.taskventure.model.task.ITaskService;
import com.m1nist3r.taskventure.model.task.Task;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskGroupServiceFirebaseImpl;
import com.m1nist3r.taskventure.model.task.TaskServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.CheckNetwork;
import com.m1nist3r.taskventure.util.FindAllTaskCallback;

import java.util.ArrayList;
import java.util.Objects;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class SelectGroupTaskActivity extends BaseActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private ITaskService taskService;
    private SelectTaskGroupAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();

        com.m1nist3r.taskventure.databinding.SelectTaskGroupBinding mBinding = SelectTaskGroupBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        ITaskGroupService taskGroupService = new TaskGroupServiceFirebaseImpl();
        taskService = new TaskServiceFirebaseImpl();

        FirestoreRecyclerOptions<TaskGroup> recyclerOptions =
                new FirestoreRecyclerOptions.Builder<TaskGroup>()
                        .setQuery(taskGroupService
                                        .findAllTaskGroups()
                                        .whereNotEqualTo("taskList", Lists.newArrayList())
                                , TaskGroup.class)
                        .build();

        mAdapter = new SelectTaskGroupAdapter(recyclerOptions, this);

        RecyclerView recyclerView = mBinding.selectTaskGroupRecyclerView;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        mBinding.selectStartGameButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();

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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.select_start_game_button) {
            if (mAdapter.getSelectedTaskGroup() == null) {
                Toast.makeText(this, "Select task group", Toast.LENGTH_LONG).show();
            } else {
                TaskGroup taskGroup = mAdapter.getSelectedTaskGroup();
                readData(taskGroup, tasks -> {
                    taskGroup.setTaskList(tasks);
                    Intent intent = new Intent();
                    intent.putExtra("taskGroup", taskGroup);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        }
    }

    public void readData(TaskGroup taskGroup, FindAllTaskCallback taskCallback) {
        taskService.findAllTasks(taskGroup).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                TaskGroup group = Objects.requireNonNull(task.getResult()).toObject(TaskGroup.class);
                ArrayList<Task> tasks = null;
                if (group != null) {
                    tasks = group.getTaskList();
                }
                taskCallback.onCallback(tasks);
            }
        });
    }
}
