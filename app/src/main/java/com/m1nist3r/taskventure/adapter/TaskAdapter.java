package com.m1nist3r.taskventure.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.model.task.ITaskService;
import com.m1nist3r.taskventure.model.task.Task;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskServiceFirebaseImpl;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final String TAG = "TASK_ADAPTER";

    private ITaskService taskServiceFirebase;
    private TaskGroup taskGroup;
    private ArrayList<Task> tasks;

    public TaskAdapter(TaskGroup taskGroup, ArrayList<Task> tasks) {
        super();
        this.taskServiceFirebase = new TaskServiceFirebaseImpl();
        this.taskGroup = taskGroup;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskAdapter.TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        holder.textViewDescription.setText(tasks.get(position).getDescription());
        long time = tasks.get(position).getTime();
        long period = tasks.get(position).getPeriod();
        if (time == 0L) {
            holder.timedTask.setVisibility(View.GONE);
        } else if (period == 0L) {
            holder.textViewTime.setText(valueOf(time));
            holder.textViewPeriod.setVisibility(View.GONE);
            holder.textViewPeriodLabel.setVisibility(View.GONE);
        } else {
            holder.textViewTime.setText(valueOf(time));
            holder.textViewPeriod.setText(valueOf(period));
        }

        holder.imageButton.setOnClickListener(view -> {
            taskServiceFirebase.deleteTask(taskGroup, tasks.get(position));
            tasks.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDescription;
        public TextView textViewTime;
        public TextView textViewPeriod;
        public TextView textViewPeriodLabel;
        public ImageButton imageButton;
        public LinearLayout timedTask;


        public TaskViewHolder(@NonNull View view) {
            super(view);
            this.imageButton = itemView.findViewById(R.id.task_delete_button);
            this.textViewDescription = itemView.findViewById(R.id.textViewTaskDescription);
            this.textViewTime = itemView.findViewById(R.id.textViewTaskTime);
            this.textViewPeriod = itemView.findViewById(R.id.textViewTaskPeriod);
            this.textViewPeriodLabel = itemView.findViewById(R.id.textViewTaskPeriodLabel);
            this.timedTask = itemView.findViewById(R.id.timed_task);
        }
    }
}
