package com.example.silentmodescheduler;

import android.app.Application;
import com.example.silentmodescheduler.di.AppContainer;
import com.example.silentmodescheduler.di.DefaultAppContainer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class SilentModeSchedulerApp extends Application {
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFirebase();
        appContainer = new DefaultAppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    private void initializeFirebase() {
        FirebaseApp.initializeApp(this);

        FirebaseFirestore.getInstance().setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                        .build()
        );
    }
}
