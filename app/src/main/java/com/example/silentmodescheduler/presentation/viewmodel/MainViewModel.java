package com.example.silentmodescheduler.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.core.firebase.FirebaseErrorMapper;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;
import com.example.silentmodescheduler.data.model.TimePeriod;
import com.example.silentmodescheduler.core.scheduler.SilentModeScheduler;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirestoreRepository firestoreRepository;
    private final SilentModeScheduler silentModeScheduler;

    private final MutableLiveData<MainUiState> uiState = new MutableLiveData<>(new MainUiState());
    private final MutableLiveData<MainEvent> events = new MutableLiveData<>();

    public MainViewModel(
            FirebaseAuthManager firebaseAuthManager,
            FirestoreRepository firestoreRepository,
            SilentModeScheduler silentModeScheduler
    ) {
        this.firebaseAuthManager = firebaseAuthManager;
        this.firestoreRepository = firestoreRepository;
        this.silentModeScheduler = silentModeScheduler;

        loadUserData();
        loadTimePeriods();
    }

    public LiveData<MainUiState> getUiState() {
        return uiState;
    }

    public LiveData<MainEvent> getEvents() {
        return events;
    }

    public void clearEvent() {
        events.setValue(null);
    }

    private void loadUserData() {
        String userId = firebaseAuthManager.getCurrentUserId();
        if (userId == null) return;

        firestoreRepository.getDocument("users", userId)
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    if (name == null) name = "User";
                    MainUiState current = uiState.getValue();
                    if (current != null) {
                        uiState.setValue(current.copy(name, null, current.isLoading(), null));
                    }
                })
                .addOnFailureListener(e -> {
                    MainUiState current = uiState.getValue();
                    if (current != null) {
                        uiState.setValue(current.copy("User", null, current.isLoading(), null));
                    }
                });
    }

    public void loadTimePeriods() {
        String userId = firebaseAuthManager.getCurrentUserId();
        if (userId == null) return;

        MainUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, null, true, null));
        }

        firestoreRepository.getCollection("users/" + userId + "/time_periods")
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TimePeriod> periods = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String start = doc.getString("startTime");
                        String end = doc.getString("endTime");
                        Boolean enabled = doc.getBoolean("enabled");
                        if (enabled == null) enabled = true;

                        periods.add(new TimePeriod(
                                doc.getId(),
                                name != null ? name : "",
                                start != null ? start : "",
                                end != null ? end : "",
                                enabled
                        ));
                    }
                    MainUiState active = uiState.getValue();
                    if (active != null) {
                        uiState.setValue(active.copy(null, periods, false, null));
                    }
                    // Evaluate current time against all schedules and trigger silent mode if matched
                    silentModeScheduler.checkAndApplyCurrentState(periods);
                })
                .addOnFailureListener(e -> {
                    MainUiState active = uiState.getValue();
                    if (active != null) {
                        uiState.setValue(active.copy(null, null, false, FirebaseErrorMapper.mapFirestoreError(e).getMessage()));
                    }
                });
    }

    public void deleteTimePeriod(String periodId) {
        String userId = firebaseAuthManager.getCurrentUserId();
        if (userId == null) return;

        // Delete from cloud in background asynchronously (fire-and-forget)
        firestoreRepository.deleteDocument("users/" + userId + "/time_periods", periodId);

        // Cancel alarms locally
        silentModeScheduler.cancelAlarms(periodId);

        // Instantly update local list and re-evaluate active ringer state
        MainUiState current = uiState.getValue();
        if (current != null) {
            List<TimePeriod> updatedList = new ArrayList<>(current.getTimePeriods());
            for (int i = 0; i < updatedList.size(); i++) {
                if (updatedList.get(i).getId().equals(periodId)) {
                    updatedList.remove(i);
                    break;
                }
            }
            uiState.setValue(current.copy(null, updatedList, false, null));
            silentModeScheduler.checkAndApplyCurrentState(updatedList);
        }

        events.setValue(new MainEvent.ShowSnackbar("Time period deleted successfully."));
    }

    public void logout() {
        MainUiState current = uiState.getValue();
        if (current != null) {
            for (TimePeriod period : current.getTimePeriods()) {
                silentModeScheduler.cancelAlarms(period.getId());
            }
        }
        firebaseAuthManager.signOut();
        events.setValue(new MainEvent.LoggedOut());
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FirebaseAuthManager firebaseAuthManager;
        private final FirestoreRepository firestoreRepository;
        private final SilentModeScheduler silentModeScheduler;

        public Factory(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository, SilentModeScheduler silentModeScheduler) {
            this.firebaseAuthManager = firebaseAuthManager;
            this.firestoreRepository = firestoreRepository;
            this.silentModeScheduler = silentModeScheduler;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MainViewModel.class)) {
                return (T) new MainViewModel(firebaseAuthManager, firestoreRepository, silentModeScheduler);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
