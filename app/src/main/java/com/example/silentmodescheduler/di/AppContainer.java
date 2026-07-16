package com.example.silentmodescheduler.di;

import com.example.silentmodescheduler.core.scheduler.SilentModeScheduler;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;

public interface AppContainer {
    FirebaseAuthManager getFirebaseAuthManager();
    FirestoreRepository getFirestoreRepository();
    SilentModeScheduler getSilentModeScheduler();
}
