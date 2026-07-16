package com.example.silentmodescheduler.di;

import android.content.Context;
import com.example.silentmodescheduler.core.scheduler.SilentModeScheduler;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.firestore.FirebaseFirestoreRepository;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;

public class DefaultAppContainer implements AppContainer {
    private final Context context;
    private FirebaseAuthManager firebaseAuthManager;
    private FirestoreRepository firestoreRepository;
    private SilentModeScheduler silentModeScheduler;

    public DefaultAppContainer(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public synchronized FirebaseAuthManager getFirebaseAuthManager() {
        if (firebaseAuthManager == null) {
            firebaseAuthManager = new FirebaseAuthManager();
        }
        return firebaseAuthManager;
    }

    @Override
    public synchronized FirestoreRepository getFirestoreRepository() {
        if (firestoreRepository == null) {
            firestoreRepository = new FirebaseFirestoreRepository();
        }
        return firestoreRepository;
    }

    @Override
    public synchronized SilentModeScheduler getSilentModeScheduler() {
        if (silentModeScheduler == null) {
            silentModeScheduler = new SilentModeScheduler(context);
        }
        return silentModeScheduler;
    }
}
