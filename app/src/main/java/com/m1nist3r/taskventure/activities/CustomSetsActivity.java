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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.TaskGroupAdapter;
import com.m1nist3r.taskventure.databinding.ActivityCustomSetsBinding;
import com.m1nist3r.taskventure.model.task.ITaskGroupService;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskGroupServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class CustomSetsActivity extends BaseActivity {

    private static final String TAG = "CUSTOM_SETS";
    private static final int RESULT_LOAD_IMAGE = 1442;

    private FirebaseAuth mAuth;
    private ActivityCustomSetsBinding mBinding;
    private ITaskGroupService taskGroupService;
    private FirestoreRecyclerAdapter<TaskGroup, TaskGroupAdapter.TaskGroupViewHolder> mAdapter;
    private StorageReference mStorageRef;
    private String uploadedImage;
    private View dialogView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();
        taskGroupService = new TaskGroupServiceFirebaseImpl();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mBinding = ActivityCustomSetsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        FloatingActionButton fab = mBinding.floatingActionButtonGroupTask;
        fab.setOnClickListener(view -> displayCreateTaskGroupAlertDialog());

        RecyclerView recyclerView = mBinding.recyclerView;
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FirestoreRecyclerOptions<TaskGroup> recyclerOptions =
                new FirestoreRecyclerOptions.Builder<TaskGroup>()
                        .setQuery(taskGroupService.findAllTaskGroups(), TaskGroup.class)
                        .build();

        mAdapter = new TaskGroupAdapter(recyclerOptions,
                getApplicationContext());
        recyclerView.setAdapter(mAdapter);
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
                });

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
}
