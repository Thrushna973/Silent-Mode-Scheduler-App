package com.example.silentmodescheduler.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.core.firebase.FirebaseErrorMapper;
import com.example.silentmodescheduler.core.scheduler.SilentModeScheduler;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;
import com.example.silentmodescheduler.data.model.TimePeriod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimePeriodViewModel extends ViewModel {
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirestoreRepository firestoreRepository;
    private final SilentModeScheduler silentModeScheduler;
    private final String timePeriodId;

    private final MutableLiveData<TimePeriodUiState> uiState;

    public TimePeriodViewModel(
            FirebaseAuthManager firebaseAuthManager,
            FirestoreRepository firestoreRepository,
            SilentModeScheduler silentModeScheduler,
            String timePeriodId
    ) {
        this.firebaseAuthManager = firebaseAuthManager;
        this.firestoreRepository = firestoreRepository;
        this.silentModeScheduler = silentModeScheduler;
        this.timePeriodId = timePeriodId;

        this.uiState = new MutableLiveData<>(new TimePeriodUiState(timePeriodId != null));

        if (timePeriodId != null) {
            loadTimePeriod(timePeriodId);
        }
    }

    public LiveData<TimePeriodUiState> getUiState() {
        return uiState;
    }

    private void loadTimePeriod(String id) {
        String userId = firebaseAuthManager.getCurrentUserId();
        if (userId == null) return;

        TimePeriodUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, null, null, null, null, null, true, false, null));
        }

        firestoreRepository.getDocument("users/" + userId + "/time_periods", id)
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    String startTime = doc.getString("startTime");
                    String endTime = doc.getString("endTime");

                    TimePeriodUiState active = uiState.getValue();
                    if (active != null) {
                        uiState.setValue(active.copy(
                                name != null ? name : "",
                                startTime != null ? startTime : "",
                                endTime != null ? endTime : "",
                                null, null, null, false, false, null
                        ));
                    }
                })
                .addOnFailureListener(e -> {
                    TimePeriodUiState active = uiState.getValue();
                    if (active != null) {
                        uiState.setValue(active.copy(
                                null, null, null, null, null, null, false, false,
                                FirebaseErrorMapper.mapFirestoreError(e).getMessage()
                        ));
                    }
                });
    }

    public void onNameChanged(String name) {
        TimePeriodUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(name, null, null, null, null, null, false, false, null));
        }
    }

    public void onStartTimeChanged(String time) {
        TimePeriodUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, time, null, null, null, null, false, false, null));
        }
    }

    public void onEndTimeChanged(String time) {
        TimePeriodUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, null, time, null, null, null, false, false, null));
        }
    }

    public void onSaveClick() {
        TimePeriodUiState current = uiState.getValue();
        if (current == null) return;

        String nameVal = current.getName().trim();
        String startVal = current.getStartTime().trim();
        String endVal = current.getEndTime().trim();

        boolean hasError = false;
        String nameErr = null;
        String startErr = null;
        String endErr = null;

        if (nameVal.isEmpty()) {
            nameErr = "Period Name is required.";
            hasError = true;
        }

        if (startVal.isEmpty()) {
            startErr = "Start Time is required.";
            hasError = true;
        }

        if (endVal.isEmpty()) {
            endErr = "End Time is required.";
            hasError = true;
        } else if (!startVal.isEmpty() && areTimesEqual(startVal, endVal)) {
            endErr = "End Time cannot be equal to Start Time.";
            hasError = true;
        }

        if (hasError) {
            uiState.setValue(current.copy(null, null, null, nameErr, startErr, endErr, false, false, null));
            return;
        }

        String userId = firebaseAuthManager.getCurrentUserId();
        if (userId == null) return;

        String id = timePeriodId != null ? timePeriodId : UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("name", nameVal);
        data.put("startTime", startVal);
        data.put("endTime", endVal);
        data.put("enabled", true);

        // Save to cloud in background asynchronously (fire-and-forget)
        firestoreRepository.setDocument("users/" + userId + "/time_periods", id, data);

        // Schedule alarms locally and apply silent mode instantly if within active period
        TimePeriod period = new TimePeriod(id, nameVal, startVal, endVal, true);
        silentModeScheduler.scheduleAlarms(period);

        // Immediately update state and notify success to close dialog
        uiState.setValue(current.copy(null, null, null, null, null, null, false, true, null));
    }

    private boolean areTimesEqual(String start, String end) {
        if (start == null || end == null) return true;
        return start.trim().equals(end.trim());
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FirebaseAuthManager firebaseAuthManager;
        private final FirestoreRepository firestoreRepository;
        private final SilentModeScheduler silentModeScheduler;
        private final String timePeriodId;

        public Factory(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository, SilentModeScheduler silentModeScheduler, String timePeriodId) {
            this.firebaseAuthManager = firebaseAuthManager;
            this.firestoreRepository = firestoreRepository;
            this.silentModeScheduler = silentModeScheduler;
            this.timePeriodId = timePeriodId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TimePeriodViewModel.class)) {
                return (T) new TimePeriodViewModel(firebaseAuthManager, firestoreRepository, silentModeScheduler, timePeriodId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
