package com.m1nist3r.taskventure.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.databinding.ActivityMainappBinding;
import com.m1nist3r.taskventure.model.game.GameServiceFirebaseImpl;
import com.m1nist3r.taskventure.model.game.IGameService;
import com.m1nist3r.taskventure.util.CheckNetwork;
import com.m1nist3r.taskventure.util.LoadGameCallback;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;


public class MainAppActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "MainApp";

    private FirebaseAuth mAuth;
    private IGameService gameService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();
        gameService = new GameServiceFirebaseImpl();

        ActivityMainappBinding mBinding = ActivityMainappBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.logOut.setOnClickListener(this);
        mBinding.myAccountButton.setOnClickListener(this);
        mBinding.myCustomSetsButton.setOnClickListener(this);
        mBinding.startGameButton.setOnClickListener(this);
        mBinding.buyPremiumSets.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isNetworkConnected && mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
            } else if (currentUser.getDisplayName() == null ||
                    currentUser.getDisplayName().equals("")) {
                displayNameChangerAlertDialog();
            }
        } else if (!isNetworkConnected) {
            AlertDialog dialog = displayMobileDataSettingsDialog(this);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private void displayNameChangerAlertDialog() {
        TextInputEditText textInputEditText = new TextInputEditText(this);
        textInputEditText.setHint("Nickname");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enter displayed name: ")
                .setTitle("Display Name")
                .setView(textInputEditText)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (textInputEditText.getText() != null && textInputEditText.getText()
                    .toString().length() > 5) {
                FirebaseUser user = mAuth.getCurrentUser();
                UserProfileChangeRequest profileUpdates =
                        new UserProfileChangeRequest.Builder()
                                .setDisplayName(textInputEditText.getText().toString())
                                .build();
                assert user != null;
                user.updateProfile(profileUpdates);
                Log.d(TAG, "displayName:success");
                dialog.dismiss();
            } else {
                textInputEditText.setError("Nickname should contain more than 5 characters");
            }
        });
    }

    public void displayStartGameAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have saved game would you like to continue?")
                .setTitle("Continue game")
                .setPositiveButton("New game", (dialogInterface, i) -> {
                })
                .setNegativeButton("Continue game", (dialogInterface, i) -> {
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            gameService.deleteGame();
            startActivity(new Intent(this, CreateGameActivity.class));
            dialog.dismiss();
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            startActivity(new Intent(this, GameActivity.class));
            dialog.dismiss();
        });
    }

    public void readData(LoadGameCallback loadGameCallback) {
        gameService.loadGame()
                .addOnCompleteListener(documentSnapshot -> loadGameCallback.onGameCallback(documentSnapshot.getResult().exists()))
                .addOnFailureListener(e -> {
                    loadGameCallback.onGameCallback(false);
                    Log.w(TAG, "GAME: game not loaded" + e.getMessage());
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.logOut) {
            this.mAuth.signOut();
            //TODO anonymous on sight out delete account if not linked.
            Log.d(TAG, "Successfully logout.");
            startActivity(new Intent(this, SignUpActivity.class));
        } else if (i == R.id.my_account_button) {
            startActivity(new Intent(this, MyAccountActivity.class));
        } else if (i == R.id.my_custom_sets_button) {
            startActivity(new Intent(this, CustomSetsActivity.class));
        } else if (i == R.id.buy_premium_sets) {
            startActivity(new Intent(this, PaymentActivity.class));
        } else if (i == R.id.start_game_button) {

            readData(gameLoaded -> {
                if (gameLoaded) {
                    displayStartGameAlertDialog();
                } else {
                    startActivity(new Intent(this, CreateGameActivity.class));
                }
            });
        }
    }
}
