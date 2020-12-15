package com.m1nist3r.taskventure.model.game;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public interface IGameService {
    void deleteGame();

    void saveGame(Game game);

    Task<DocumentSnapshot> loadGame();
}
