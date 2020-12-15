package com.m1nist3r.taskventure.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.m1nist3r.taskventure.fragments.OverallChartFragment;
import com.m1nist3r.taskventure.fragments.PlayerCharFragment;
import com.m1nist3r.taskventure.model.player.Player;

import java.util.List;

public class ChartCollectionAdapter extends FragmentStateAdapter {
    private final List<Player> playerList;

    public ChartCollectionAdapter(FragmentActivity fm, List<Player> playerList) {
        super(fm);
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            OverallChartFragment overallChartFragment = new OverallChartFragment();
            overallChartFragment.setPlayerList(playerList);
            return overallChartFragment;
        }
        PlayerCharFragment playerCharFragment = new PlayerCharFragment();
        playerCharFragment.setPlayer(playerList.get(position - 1));
        return playerCharFragment;
    }

    @Override
    public int getItemCount() {
        return playerList.size() + 1;
    }
}
