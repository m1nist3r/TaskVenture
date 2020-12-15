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
import com.m1nist3r.taskventure.model.task.TaskGroupBuyable;
import com.m1nist3r.taskventure.util.ItemClickListener;

public class TaskGroupBuyableAdapter extends FirestoreRecyclerAdapter<TaskGroupBuyable, TaskGroupBuyableAdapter.TaskGroupBuyableViewHolder> {
    private static final String TAG = "TASK_GROUP_ADAPTER";

    private final Context context;
    private int selectedPos = -1;


    public TaskGroupBuyableAdapter(FirestoreRecyclerOptions<TaskGroupBuyable> recyclerOptions, Context context) {
        super(recyclerOptions);
        this.context = context;
    }

    @NonNull
    @Override
    public TaskGroupBuyableAdapter.TaskGroupBuyableViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_group_buyable_item, parent, false);
        return new TaskGroupBuyableAdapter.TaskGroupBuyableViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskGroupBuyableAdapter.TaskGroupBuyableViewHolder holder, int position,
                                    @NonNull TaskGroupBuyable model) {
        holder.taskGroupName.setText(model.getName());
        holder.taskGroupDescription.setText(model.getDescription());
        holder.taskGroupPrice.setText(model.getPrice() + "$");

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
                    .into(holder.taskGroupImage);

        } catch (IllegalArgumentException e) {
            Log.d(TAG, "image_link_wrong: " + e.getMessage() + "\n" + model.getImagePath());
        }

        holder.setItemClickListener((v, pos) -> {
            Log.w(TAG, "onBindViewHolder: ", new Exception("test"));
            if (selectedPos != position) {
                notifyItemChanged(selectedPos);
                selectedPos = position;
                holder.checkBox.setChecked(true);
            }
        });

        if (selectedPos != position) {
            holder.checkBox.setChecked(false);
        }
    }

    public TaskGroupBuyable getSelectedTaskGroup() {
        if (selectedPos == -1) return null;
        return getSnapshots().get(selectedPos);
    }

    public static class TaskGroupBuyableViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        public TextView taskGroupName;
        public ImageView taskGroupImage;
        public TextView taskGroupDescription;
        public TextView taskGroupPrice;
        public CheckBox checkBox;

        ItemClickListener itemClickListener;

        public TaskGroupBuyableViewHolder(@NonNull View view) {
            super(view);
            this.taskGroupName = itemView.findViewById(R.id.textViewTaskGroupBuy);
            this.taskGroupImage = itemView.findViewById(R.id.imageViewTaskGroupBuy);
            this.taskGroupDescription = itemView.findViewById(R.id.detailDescription);
            this.taskGroupPrice = itemView.findViewById(R.id.detailPrice);
            this.checkBox = itemView.findViewById(R.id.checkBoxTaskGroupBuyable);

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
