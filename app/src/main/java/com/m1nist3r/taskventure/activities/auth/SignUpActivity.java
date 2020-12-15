package com.m1nist3r.taskventure.activities.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.BaseActivity;
import com.m1nist3r.taskventure.activities.MainAppActivity;
import com.m1nist3r.taskventure.databinding.ActivityEmailpasswordBinding;
import com.m1nist3r.taskventure.util.CheckNetwork;

import java.util.Objects;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class SignUpActivity extends BaseActivity
        implements View.OnClickListener {

    private static final String TAG = "EmailPassword";
    private static final int RC_SIGN_IN = 9001;

    private ActivityEmailpasswordBinding mBinding;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mBinding = ActivityEmailpasswordBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.emailSignInButton.setOnClickListener(this);
        mBinding.emailCreateAccountButton.setOnClickListener(this);
        mBinding.loginAnonymousSignIn.setOnClickListener(this);
        mBinding.loginResetPassword.setOnClickListener(this);
        mBinding.signInGoogle.setOnClickListener(this);

    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(new Intent(this, MainAppActivity.class));
                        Log.d(TAG, "signInWithCredential:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(mBinding.mainLayout, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()),
                                Snackbar.LENGTH_SHORT).show();
                    }

                    hideProgressBar();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + Objects.requireNonNull(account).getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(SignUpActivity.this,
                        Objects.requireNonNull(e.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isNetworkConnected && mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                startActivity(new Intent(this, MainAppActivity.class));
            }
        } else if (!isNetworkConnected) {
            AlertDialog dialog = displayMobileDataSettingsDialog(this);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (validateForm()) {
            return;
        }

        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        Toast.makeText(SignUpActivity.this,
                                "User successfully registered.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this,
                                Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    hideProgressBar();
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (validateForm()) {
            return;
        }

        showProgressBar();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        startActivity(new Intent(this, MainAppActivity.class));
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this,
                                Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    hideProgressBar();
                });

    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signInAnonymously() {

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        startActivity(new Intent(this, MainAppActivity.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideProgressBar();
                });
    }


    private void forgotPassword(String email) {
        if (validateEmail()) {
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent.");
                        Toast.makeText(SignUpActivity.this,
                                "Password reset email successfully send to your email address."
                                , Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Email not sent.");
                        Toast.makeText(SignUpActivity.this,
                                Objects.requireNonNull(task.getException()).getMessage()
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = Objects.requireNonNull(mBinding.fieldEmail.getEditText()).getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldEmail.setError(null);
        }

        String password = Objects.requireNonNull(mBinding.fieldPassword.getEditText()).getText().toString();
        if (TextUtils.isEmpty(password)) {
            mBinding.fieldPassword.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldPassword.setError(null);
        }

        return !valid;
    }

    private boolean validateEmail() {
        boolean valid = true;

        String email = Objects.requireNonNull(mBinding.fieldEmail.getEditText()).getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldEmail.setError(null);
        }

        return !valid;
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            createAccount(Objects.requireNonNull(mBinding.fieldEmail
                            .getEditText())
                            .getText()
                            .toString(),
                    Objects.requireNonNull(mBinding.fieldPassword.
                            getEditText())
                            .getText()
                            .toString());
        } else if (i == R.id.emailSignInButton) {
            signIn(Objects.requireNonNull(mBinding.fieldEmail
                            .getEditText())
                            .getText()
                            .toString(),
                    Objects.requireNonNull(mBinding.fieldPassword.
                            getEditText())
                            .getText()
                            .toString());
        } else if (i == R.id.loginAnonymousSignIn) {
            signInAnonymously();
        } else if (i == R.id.loginResetPassword) {
            forgotPassword(Objects.requireNonNull(mBinding.fieldEmail
                    .getEditText())
                    .getText()
                    .toString());
        } else if (i == R.id.sign_in_google) {
            signInGoogle();
        }
    }
}
