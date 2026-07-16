package com.example.silentmodescheduler.presentation.viewmodel;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.core.firebase.FirebaseErrorMapper;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.auth.PhoneOtpSession;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;
import com.example.silentmodescheduler.presentation.auth.RegistrationValidator;
import com.google.firebase.auth.PhoneAuthCredential;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirestoreRepository firestoreRepository;
    private final MutableLiveData<LoginUiState> uiState = new MutableLiveData<>(new LoginUiState());
    private final MutableLiveData<LoginEvent> events = new MutableLiveData<>();

    public LoginViewModel(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository) {
        this.firebaseAuthManager = firebaseAuthManager;
        this.firestoreRepository = firestoreRepository;
    }

    public LiveData<LoginUiState> getUiState() {
        return uiState;
    }

    public LiveData<LoginEvent> getEvents() {
        return events;
    }

    public void clearEvent() {
        events.setValue(null);
    }

    public void onPhoneNumberChanged(String phoneNumber) {
        LoginUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(phoneNumber, null, null, false));
        }
    }

    public void onSendOtpClick(Activity activity) {
        LoginUiState current = uiState.getValue();
        if (current == null || current.isLoading()) return;

        RegistrationValidator.PhoneValidationResult validation =
                RegistrationValidator.validatePhoneNumber(current.getPhoneNumber());

        if (!validation.isValid) {
            uiState.setValue(current.copy(null, validation.error, null, false));
            return;
        }

        uiState.setValue(current.copy(null, null, null, true));

        // Query Firestore to verify the user is registered
        firestoreRepository.getCollectionWhereEqualTo("users", "phoneNumber", validation.normalizedPhoneNumber)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        LoginUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, "Phone number not registered. Please register first.", null, false));
                        }
                    } else {
                        // User exists: Send OTP
                        sendOtpCode(activity, validation.normalizedPhoneNumber);
                    }
                })
                .addOnFailureListener(e -> {
                    LoginUiState active = uiState.getValue();
                    if (active != null) {
                        String errorMessage = FirebaseErrorMapper.mapFirestoreError(e).getMessage();
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("offline")) {
                            errorMessage = "Internet connection required to verify account. Please check your connection.";
                        }
                        uiState.setValue(active.copy(null, null, errorMessage, false));
                    }
                });
    }

    private void sendOtpCode(Activity activity, String normalizedPhone) {
        firebaseAuthManager.sendPhoneVerificationCode(
                activity,
                normalizedPhone,
                new FirebaseAuthManager.PhoneVerificationCallback() {
                    @Override
                    public void onCodeSent(PhoneOtpSession session) {
                        LoginUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, null, false));
                        }
                        events.setValue(new LoginEvent.OtpSent(normalizedPhone, session.getVerificationId()));
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        LoginUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, "Phone number verified automatically. Please continue.", false));
                        }
                    }

                    @Override
                    public void onVerificationFailed(String errorMessage) {
                        LoginUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, errorMessage, false));
                        }
                    }
                }
        );
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FirebaseAuthManager firebaseAuthManager;
        private final FirestoreRepository firestoreRepository;

        public Factory(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository) {
            this.firebaseAuthManager = firebaseAuthManager;
            this.firestoreRepository = firestoreRepository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(LoginViewModel.class)) {
                return (T) new LoginViewModel(firebaseAuthManager, firestoreRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
