package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.databinding.ActivityMyAccountBinding;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.util.Objects;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class MyAccountActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "MyAccount";

    private ActivityMyAccountBinding mBinding;
    private FirebaseAuth mAuth;


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.save_name_button) {
            changeNickname(mBinding.editName.getText().toString());
        } else if (i == R.id.my_account_back_button) {
            startActivity(new Intent(this, MainAppActivity.class));
        }
    }

    private void changeNickname(String nickname) {
        if (nickname != null && nickname.length() > 5 &&
                !nickname.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName())) {
            FirebaseUser user = mAuth.getCurrentUser();
            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(nickname)
                            .build();
            assert user != null;
            user.updateProfile(profileUpdates);
            Log.d(TAG, "displayName:success");
            Toast.makeText(this,
                    "Nickname changed", Toast.LENGTH_LONG).show();
        } else {
            mBinding.editName.setError("Enter nickname");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();

        mBinding = ActivityMyAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());


        mBinding.editName.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());

        mBinding.saveNameButton.setOnClickListener(this);
        mBinding.myAccountBackButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
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

