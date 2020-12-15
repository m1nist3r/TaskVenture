package com.m1nist3r.taskventure.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

public class CheckNetwork {
    private ConnectivityManager connectivityManager;

    public CheckNetwork(Context context) {
        this.connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void registerNetworkCallback() {
        try {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerNetworkCallback(builder.build(),
                    new ConnectivityManager.NetworkCallback() {

                        @Override
                        public void onAvailable(@NonNull Network network) {
                            GlobalVariables.isNetworkConnected = true; // Global Static Variable
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            GlobalVariables.isNetworkConnected = false; // Global Static Variable
                        }
                    }

            );
            GlobalVariables.isNetworkConnected = false;
        } catch (Exception e) {
            GlobalVariables.isNetworkConnected = false;
        }
    }

    public void stopNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(new ConnectivityManager.NetworkCallback());
    }
}
