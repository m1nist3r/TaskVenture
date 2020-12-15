package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.AddTaskToGroupAdapter;
import com.m1nist3r.taskventure.databinding.ActivityAddTaskToGroupBinding;
import com.m1nist3r.taskventure.model.task.ITaskGroupService;
import com.m1nist3r.taskventure.model.task.ITaskService;
import com.m1nist3r.taskventure.model.task.Task;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskGroupServiceFirebaseImpl;
import com.m1nist3r.taskventure.model.task.TaskServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class AddTaskToGroupActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ADD_TASK_TO_GROUP";
    private static final int RESULT_LOAD_IMAGE = 1442;

    private FirebaseAuth mAuth;
    private ITaskService taskService;
    private ITaskGroupService taskGroupService;
    private AddTaskToGroupAdapter mAdapter;
    private Task task;
    private View dialogView;
    private ActivityAddTaskToGroupBinding mBinding;
    private String uploadedImage;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();

        mBinding = ActivityAddTaskToGroupBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        taskService = new TaskServiceFirebaseImpl();
        taskGroupService = new TaskGroupServiceFirebaseImpl();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        FloatingActionButton fab = mBinding.floatingActionButtonGroupTaskAdd;
        fab.setOnClickListener(view -> displayCreateTaskGroupAlertDialog());

        FirestoreRecyclerOptions<TaskGroup> recyclerOptions =
                new FirestoreRecyclerOptions.Builder<TaskGroup>()
                        .setQuery(taskGroupService.findAllTaskGroups(), TaskGroup.class)
                        .build();

        mAdapter = new AddTaskToGroupAdapter(recyclerOptions, this);

        RecyclerView recyclerView = mBinding.recyclerViewAdd;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            task = bundle.getParcelable("task");
        } else {
            finish();
        }

        mBinding.addToCustomButton.setOnClickListener(this);
    }

    private void displayCreateTaskGroupAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.add_task_group, mBinding.getRoot(), false);
        builder.setView(dialogView);

        EditText textInputEditText = dialogView.findViewById(R.id.task_group_name);
        Button button = dialogView.findViewById(R.id.upload_task_photo);

        button.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
            button.setClickable(false);
        });

        builder.setMessage("Enter task group name: ")
                .setTitle("Task Group")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (textInputEditText.getText() != null && textInputEditText.getText()
                    .toString().length() > 5) {
                TaskGroup taskGroup = new TaskGroup();
                taskGroup.setId(UUID.randomUUID().toString());
                taskGroup.setName(textInputEditText.getText().toString());
                taskGroup.setTaskList(new ArrayList<>());
                if (uploadedImage != null) {
                    taskGroup.setImagePath(uploadedImage);
                } else {
                    taskGroup.setImagePath("gs://taskventure-c7a17.appspot.com/task_group_image/baseline_extension_black_48.png");
                }
                taskGroupService.saveTask(taskGroup);
                Log.d(TAG, "Task group created");
                dialog.dismiss();
            } else {
                textInputEditText.setError("Task group name should contain more than 5 characters");
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        Button button = dialogView.findViewById(R.id.upload_task_photo);

        if (resultCode == RESULT_OK) {
            button.setText(R.string.ok);
            button.setClickable(true);

            final Uri image = data.getData();

            File file = new File(Objects.requireNonNull(Objects.requireNonNull(image).getPath()));
            String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));

            String fileName = UUID.randomUUID().toString();

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/*")
                    .build();
            StorageReference taskRef = mStorageRef.child("task_group_image/"
                    + fileName + extension);

            taskRef.putFile(Objects.requireNonNull(image), metadata)
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "Upload is " + progress + "% done");
                    })
                    .addOnPausedListener(taskSnapshot -> Log.d(TAG, "Upload is paused"))
                    .addOnFailureListener(exception -> {
                        uploadedImage = "gs://taskventure-c7a17.appspot.com/task_group_image/baseline_extension_black_48.png";
                        Log.d(TAG, "Upload failed");
                    })
                    .addOnCompleteListener(taskSnapshot -> {
                        uploadedImage = "gs://taskventure-c7a17.appspot.com/task_group_image/" + fileName + extension;
                        Log.d(TAG, "Upload success");
                    });
        } else {
            button.setText(R.string.failed);
            button.setClickable(true);
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        if (i == mBinding.addToCustomButton.getId()) {
            if (mAdapter.getSelectedTaskGroup() == null) {
                Toast.makeText(this, "Select task group", Toast.LENGTH_LONG).show();
            } else {
                TaskGroup taskGroup = mAdapter.getSelectedTaskGroup();
                taskService.saveTask(taskGroup, task);
                Toast.makeText(this, "Task saved to: " + taskGroup.getName(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
