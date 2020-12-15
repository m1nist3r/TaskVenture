package com.m1nist3r.taskventure.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.CustomTasksActivity;
import com.m1nist3r.taskventure.model.task.ITaskGroupService;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskGroupServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.ItemClickListener;


public class TaskGroupAdapter extends FirestoreRecyclerAdapter<TaskGroup, TaskGroupAdapter.TaskGroupViewHolder> {
    private static final String TAG = "TASK_GROUP_ADAPTER";

    private Context context;
    private ITaskGroupService taskGroupServiceFirebase;

    public TaskGroupAdapter(FirestoreRecyclerOptions<TaskGroup> recyclerOptions, Context context) {
        super(recyclerOptions);
        this.context = context;
        this.taskGroupServiceFirebase = new TaskGroupServiceFirebaseImpl();
    }

    @NonNull
    @Override
    public TaskGroupAdapter.TaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_group_item, parent, false);
        return new TaskGroupViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskGroupViewHolder holder, int position,
                                    @NonNull TaskGroup model) {
        holder.textView.setText(model.getName());

        try {
            StorageReference mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getImagePath());

            Log.d(TAG, "image_download_success" + "\n" + model.getImagePath());

            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
            circularProgressDrawable.setStrokeWidth(5f);
            circularProgressDrawable.setCenterRadius(30f);
            circularProgressDrawable.start();

            RequestOptions requestOption = new RequestOptions()
                    .placeholder(circularProgressDrawable)
                    .error(R.drawable.baseline_clear_black_24)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform();

            Glide.with(context)
                    .load(mStorageReference)
                    .apply(requestOption)
                    .into(holder.imageView);

        } catch (IllegalArgumentException e) {
            Log.d(TAG, "image_link_wrong: " + e.getMessage() + "\n" + model.getImagePath());
        }

        holder.setItemClickListener((v, pos) -> {
            Intent intent = new Intent(v.getContext(), CustomTasksActivity.class);
            intent.putExtra("taskGroup", model);
            v.getContext().startActivity(intent);
        });

        holder.imageButton.setOnClickListener(view -> {
            taskGroupServiceFirebase.deleteTask(model);
            Log.d(TAG, "task_group_deleted with ID: " + model.getId() + ", and Name: " + model.getName());
        });
    }

    public static class TaskGroupViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView textView;
        public ImageView imageView;
        public ImageButton imageButton;

        ItemClickListener itemClickListener;

        public TaskGroupViewHolder(@NonNull View view) {
            super(view);
            this.imageButton = itemView.findViewById(R.id.task_group_delete_button);
            this.imageView = itemView.findViewById(R.id.imageViewTaskGroup);
            this.textView = itemView.findViewById(R.id.textViewTaskGroup);

            itemView.setOnClickListener(this);
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.itemClickListener.onItemClick(view, getLayoutPosition());
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }
}
