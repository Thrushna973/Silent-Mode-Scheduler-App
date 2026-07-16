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

public class RegisterViewModel extends ViewModel {
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirestoreRepository firestoreRepository;
    private final MutableLiveData<RegisterUiState> uiState = new MutableLiveData<>(new RegisterUiState());
    private final MutableLiveData<RegisterEvent> events = new MutableLiveData<>();

    public RegisterViewModel(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository) {
        this.firebaseAuthManager = firebaseAuthManager;
        this.firestoreRepository = firestoreRepository;
    }

    public LiveData<RegisterUiState> getUiState() {
        return uiState;
    }

    public LiveData<RegisterEvent> getEvents() {
        return events;
    }

    public void clearEvent() {
        events.setValue(null);
    }

    public void onNameChanged(String name) {
        RegisterUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(name, null, null, null, null, false));
        }
    }

    public void onPhoneNumberChanged(String phoneNumber) {
        RegisterUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, phoneNumber, null, null, null, false));
        }
    }

    public void onSendOtpClick(Activity activity) {
        RegisterUiState current = uiState.getValue();
        if (current == null || current.isLoading()) return;

        RegistrationValidator.RegistrationValidationResult validation =
                RegistrationValidator.validate(current.getName(), current.getPhoneNumber());

        if (!validation.isValid) {
            uiState.setValue(current.copy(null, null, validation.nameError, validation.phoneNumberError, null, false));
            return;
        }

        uiState.setValue(current.copy(null, null, null, null, null, true));

        // Query Firestore to verify the user does not exist yet
        firestoreRepository.getCollectionWhereEqualTo("users", "phoneNumber", validation.normalizedPhoneNumber)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        RegisterUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, null, "Phone number already registered. Please login instead.", null, false));
                        }
                    } else {
                        // User does not exist: Send OTP
                        sendOtpCode(activity, validation.normalizedName, validation.normalizedPhoneNumber);
                    }
                })
                .addOnFailureListener(e -> {
                    RegisterUiState active = uiState.getValue();
                    if (active != null) {
                        String errorMessage = FirebaseErrorMapper.mapFirestoreError(e).getMessage();
                        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("offline")) {
                            errorMessage = "Internet connection required to register. Please check your connection.";
                        }
                        uiState.setValue(active.copy(null, null, null, null, errorMessage, false));
                    }
                });
    }

    private void sendOtpCode(Activity activity, String normalizedName, String normalizedPhone) {
        firebaseAuthManager.sendPhoneVerificationCode(
                activity,
                normalizedPhone,
                new FirebaseAuthManager.PhoneVerificationCallback() {
                    @Override
                    public void onCodeSent(PhoneOtpSession session) {
                        RegisterUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, null, null, null, false));
                        }
                        events.setValue(new RegisterEvent.OtpSent(
                                normalizedName,
                                normalizedPhone,
                                session.getVerificationId()
                        ));
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        RegisterUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, null, null, "Phone number verified automatically. Please continue from OTP verification.", false));
                        }
                    }

                    @Override
                    public void onVerificationFailed(String errorMessage) {
                        RegisterUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, null, null, null, errorMessage, false));
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
            if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
                return (T) new RegisterViewModel(firebaseAuthManager, firestoreRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
