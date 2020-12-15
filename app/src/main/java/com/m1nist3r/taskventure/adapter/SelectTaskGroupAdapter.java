package com.m1nist3r.taskventure.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.util.ItemClickListener;

public class SelectTaskGroupAdapter extends FirestoreRecyclerAdapter<TaskGroup, SelectTaskGroupAdapter.SelectTaskGroupViewHolder> {
    private static final String TAG = "TASK_GROUP_ADAPTER";
    FirestoreRecyclerOptions<TaskGroup> recyclerOptions;
    private final Context context;
    private int selectedPos = -1;


    public SelectTaskGroupAdapter(FirestoreRecyclerOptions<TaskGroup> recyclerOptions, Context context) {
        super(recyclerOptions);
        this.recyclerOptions = recyclerOptions;
        this.context = context;
    }

    @NonNull
    @Override
    public SelectTaskGroupAdapter.SelectTaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_task_group_item, parent, false);
        return new SelectTaskGroupAdapter.SelectTaskGroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectTaskGroupViewHolder holder, int position,
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
            if (selectedPos != position) {
                notifyItemChanged(selectedPos);
                selectedPos = position;
                CheckBox checkBox = v.findViewById(R.id.checkBoxSelectTaskGroup);
                checkBox.setChecked(true);
            }
        });

        if (selectedPos != position) {
            holder.checkBox.setChecked(false);
        }
    }

    public TaskGroup getSelectedTaskGroup() {
        if (selectedPos == -1) return null;
        return recyclerOptions.getSnapshots().get(selectedPos);
    }

    public static class SelectTaskGroupViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView textView;
        public ImageView imageView;
        public CheckBox checkBox;

        ItemClickListener itemClickListener;

        public SelectTaskGroupViewHolder(@NonNull View view) {
            super(view);
            this.checkBox = itemView.findViewById(R.id.checkBoxSelectTaskGroup);
            this.imageView = itemView.findViewById(R.id.imageViewSelectTaskGroup);
            this.textView = itemView.findViewById(R.id.textViewSelectTaskGroup);

            itemView.setOnClickListener(this);
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
