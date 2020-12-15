package com.m1nist3r.taskventure.model.game;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.m1nist3r.taskventure.db.DriverManager;

import java.util.Objects;

public class GameServiceFirebaseImpl implements IGameService {
    private static final String TAG = "GameServiceFirebase";
    private static final String PLAYER = "player";
    private static final String GAME = "player";

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public GameServiceFirebaseImpl() {
        this.db = DriverManager.getConnection();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void deleteGame() {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GAME)
                .document("game")
                .delete()
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Game was deleted"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error deleting game", e));
    }

    @Override
    public void saveGame(Game game) {
        db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GAME)
                .document("game")
                .set(game)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Game successfully saved."))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error saving game.", e));
    }

    @Override
    public Task<DocumentSnapshot> loadGame() {
        return db
                .collection(PLAYER)
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection(GAME)
                .document("game")
                .get();
    }
}
