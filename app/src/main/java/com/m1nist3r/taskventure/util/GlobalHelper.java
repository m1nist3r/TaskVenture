package com.m1nist3r.taskventure.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import static android.provider.Settings.ACTION_WIRELESS_SETTINGS;

public class GlobalHelper {

    public static AlertDialog displayMobileDataSettingsDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("No Internet");
        builder.setMessage("Please connect to your internet");
        builder.setPositiveButton("Wifi", (dialog, which) -> {
            activity.startActivity(new Intent(ACTION_WIRELESS_SETTINGS));
            activity.finish();
        });

        return builder.create();
    }
}
