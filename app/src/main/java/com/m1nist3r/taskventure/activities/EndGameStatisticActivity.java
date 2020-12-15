package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFormatException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.ChartCollectionAdapter;
import com.m1nist3r.taskventure.databinding.ActivityEndGameStatisticBinding;
import com.m1nist3r.taskventure.model.player.Player;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.util.List;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class EndGameStatisticActivity extends FragmentActivity {

    private static final String TAG = "EndGameStatistic";

    private FirebaseAuth mAuth;

    private List<Player> playerList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckNetwork checkNetwork = new CheckNetwork(this);
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();

        com.m1nist3r.taskventure.databinding.ActivityEndGameStatisticBinding mBinding = ActivityEndGameStatisticBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        TabLayout tabLayout = mBinding.tabLayout;
        ViewPager2 viewPager = mBinding.pager;

        Bundle bundle = getIntent().getExtras();

        try {
            playerList = bundle.getParcelableArrayList("playerList");
        } catch (ParcelFormatException e) {
            Log.w(TAG, "GAME: task group and player list parcel exception.\n"
                    + e.getMessage());
        }

        ChartCollectionAdapter chartCollectionAdapter = new ChartCollectionAdapter(this, playerList);

        viewPager.setAdapter(chartCollectionAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0)
                        tab.setText("Overall");
                    else {
                        tab.setText(playerList.get(position - 1).getName());
                    }
                }
        ).attach();
    }

    @Override
    public void onStart() {
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

}
