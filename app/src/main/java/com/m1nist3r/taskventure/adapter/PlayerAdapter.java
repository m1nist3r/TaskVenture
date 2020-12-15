package com.m1nist3r.taskventure.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.material.textfield.TextInputEditText;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.model.player.Player;

import java.util.List;

public class PlayerAdapter extends Adapter<PlayerAdapter.PlayerViewHolder> {
    private static final String TAG = "PLAYER_ADAPTER";

    private List<Player> playerList;
    private Context context;

    public PlayerAdapter(Context context, List<Player> playerList) {
        super();
        this.playerList = playerList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlayerAdapter.PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_player_item, parent, false);
        return new PlayerAdapter.PlayerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        holder.playerName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                playerList.get(position).setName(s.toString());
            }
        });

        holder.imageButton.setOnClickListener(view -> {
            if (getItemCount() <= 2) {
                Toast.makeText(context, "Minimum 2 players", Toast.LENGTH_LONG).show();
                return;
            }
            playerList.remove(position - 1);
            notifyItemRemoved(position);
            Log.d(TAG, "player with name: " + playerList.get(position - 1).getName()
                    + " deleted");
        });
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        public TextInputEditText playerName;
        public ImageButton imageButton;

        public PlayerViewHolder(@NonNull View view) {
            super(view);
            this.playerName = itemView.findViewById(R.id.text_input_add_player);
            this.imageButton = itemView.findViewById(R.id.player_delete_button);
        }
    }
}
