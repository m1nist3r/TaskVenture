package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.TaskAdapter;
import com.m1nist3r.taskventure.databinding.ActivityCustomTasksBinding;
import com.m1nist3r.taskventure.model.task.ITaskService;
import com.m1nist3r.taskventure.model.task.Task;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.util.ArrayList;
import java.util.UUID;

import static com.google.common.primitives.Longs.tryParse;
import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class CustomTasksActivity extends BaseActivity {

    private static final String TAG = "CUSTOM_TASKS";

    private FirebaseAuth mAuth;
    private ActivityCustomTasksBinding mBinding;
    private RecyclerView.Adapter<TaskAdapter.TaskViewHolder> mAdapter;
    private ITaskService taskService;
    private TaskGroup taskGroup;
    private ArrayList<Task> tasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();
        taskService = new TaskServiceFirebaseImpl();
        taskGroup = getIntent().getParcelableExtra("taskGroup");

        if (taskGroup != null) {
            tasks = taskGroup.getTaskList();
        } else {
            startActivity(new Intent(this, MainAppActivity.class));
        }

        mBinding = ActivityCustomTasksBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        FloatingActionButton fab = mBinding.floatingActionButtonTask;
        fab.setOnClickListener(view -> displayCreateTaskGroupAlertDialog());

        RecyclerView recyclerView = mBinding.recyclerViewTask;
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskAdapter(taskGroup, tasks);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
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

    private void displayCreateTaskGroupAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_task, mBinding.getRoot(), false);
        builder.setView(dialogView);

        EditText taskDescription = dialogView.findViewById(R.id.task_description);
        EditText taskTime = dialogView.findViewById(R.id.task_time);
        EditText periodTime = dialogView.findViewById(R.id.task_period_time);

        builder.setMessage("Enter task description: ")
                .setTitle("Task")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (taskDescription.getText() != null && taskDescription.getText()
                    .toString().length() > 10) {
                Task task = new Task();
                task.setId(UUID.randomUUID().toString());
                task.setDescription(taskDescription.getText().toString());
                if (!taskTime.getText().toString().equals("")) {
                    Long time = tryParse(taskTime.getText().toString());
                    if (time != null) {
                        task.setTime(time);
                    } else {
                        task.setTime(0L);
                    }
                }
                if (!periodTime.getText().toString().equals("")) {
                        Long period = tryParse(periodTime.getText().toString());
                        if (period != null && task.getTime() % period == 0 && period >= 10) {
                            task.setPeriod(period);
                        } else if (period == null){
                            task.setPeriod(0L);
                        } else if (task.getTime() % period != 0){
                            Toast.makeText(this, "Period cannot be longer than time!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(this, "Minimum period time is 10 seconds!", Toast.LENGTH_SHORT).show();
                        }
                }
                taskService.saveTask(taskGroup, task);
                tasks.add(task);
                mAdapter.notifyItemInserted(mAdapter.getItemCount() + 1);
                Log.d(TAG, "Task created");
                dialog.dismiss();
            } else {
                taskDescription.setError("Task description should contain more than 20 characters");
            }
        });
    }
}
