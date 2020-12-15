package com.m1nist3r.taskventure.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.model.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OverallChartFragment extends Fragment {

    private List<Player> playerList;

    public OverallChartFragment() {

    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AnyChartView anyChartView = view.findViewById(R.id.any_chart);
        anyChartView.setProgressBar(view.findViewById(R.id.progressBar));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        playerList.forEach(player -> data.add(new ValueDataEntry(player.getName(), player.getScore())));

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title("Players score");

        cartesian.yScale().minimum(getMinValue());
        cartesian.yScale().maximum(getMaxValue());
        cartesian.yScale().minimumGap(1d);
        cartesian.yScale().maximumGap(1d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Player");
        cartesian.yAxis(0).title("Score");

        anyChartView.setChart(cartesian);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.chart_fragment, container, false);
    }

    private Number getMinValue() {
        return playerList.stream()
                .min(Comparator.comparing(Player::getScore))
                .map(Player::getScore)
                .orElseThrow(RuntimeException::new);
    }

    private Number getMaxValue() {
        return playerList.stream()
                .max(Comparator.comparing(Player::getScore))
                .map(Player::getScore)
                .orElseThrow(RuntimeException::new);
    }
}
