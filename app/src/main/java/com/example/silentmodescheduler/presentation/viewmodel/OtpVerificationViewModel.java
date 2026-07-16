package com.example.silentmodescheduler.presentation.viewmodel;

import android.app.Activity;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.silentmodescheduler.core.firebase.FirebaseErrorMapper;
import com.example.silentmodescheduler.data.firebase.auth.FirebaseAuthManager;
import com.example.silentmodescheduler.data.firebase.auth.PhoneOtpSession;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.example.silentmodescheduler.data.firebase.firestore.FirestoreRepository;
import java.util.HashMap;
import java.util.Map;

public class OtpVerificationViewModel extends ViewModel {
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirestoreRepository firestoreRepository;
    private final String registrationName;
    private final String phoneNumber;
    private String verificationId;

    private final MutableLiveData<OtpVerificationUiState> uiState = new MutableLiveData<>(new OtpVerificationUiState());
    private final MutableLiveData<OtpVerificationEvent> events = new MutableLiveData<>();
    private CountDownTimer countDownTimer;

    public OtpVerificationViewModel(
            FirebaseAuthManager firebaseAuthManager,
            FirestoreRepository firestoreRepository,
            String registrationName,
            String phoneNumber,
            String initialVerificationId
    ) {
        this.firebaseAuthManager = firebaseAuthManager;
        this.firestoreRepository = firestoreRepository;
        this.registrationName = registrationName;
        this.phoneNumber = phoneNumber;
        this.verificationId = initialVerificationId;

        startCountdownTimer();
    }

    public LiveData<OtpVerificationUiState> getUiState() {
        return uiState;
    }

    public LiveData<OtpVerificationEvent> getEvents() {
        return events;
    }

    public void clearEvent() {
        events.setValue(null);
    }

    public void onOtpCodeChanged(String otpCode) {
        if (otpCode.length() <= 6 && isDigitsOnly(otpCode)) {
            OtpVerificationUiState current = uiState.getValue();
            if (current != null) {
                uiState.setValue(current.copy(otpCode, -1, current.isResendEnabled(), current.isLoading(), null));
            }
        }
    }

    private boolean isDigitsOnly(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private void startCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        OtpVerificationUiState current = uiState.getValue();
        if (current != null) {
            uiState.setValue(current.copy(null, 30, false, current.isLoading(), null));
        }

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                OtpVerificationUiState active = uiState.getValue();
                if (active != null) {
                    uiState.setValue(active.copy(null, seconds, false, active.isLoading(), null));
                }
            }

            @Override
            public void onFinish() {
                OtpVerificationUiState active = uiState.getValue();
                if (active != null) {
                    uiState.setValue(active.copy(null, 0, true, active.isLoading(), null));
                }
            }
        }.start();
    }

    public void onResendOtpClick(Activity activity) {
        OtpVerificationUiState current = uiState.getValue();
        if (current == null || !current.isResendEnabled() || current.isLoading()) return;

        uiState.setValue(current.copy(null, -1, false, true, null));

        firebaseAuthManager.sendPhoneVerificationCode(
                activity,
                phoneNumber,
                new FirebaseAuthManager.PhoneVerificationCallback() {
                    @Override
                    public void onCodeSent(PhoneOtpSession session) {
                        verificationId = session.getVerificationId();
                        OtpVerificationUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, -1, false, false, null));
                        }
                        startCountdownTimer();
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        OtpVerificationUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, -1, false, false, "Phone number verified automatically. Click Verify to continue."));
                        }
                    }

                    @Override
                    public void onVerificationFailed(String errorMessage) {
                        OtpVerificationUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, -1, true, false, errorMessage));
                        }
                    }
                }
        );
    }

    public void onVerifyOtpClick() {
        OtpVerificationUiState current = uiState.getValue();
        if (current == null || current.isLoading()) return;

        if (current.getOtpCode().length() != 6) {
            uiState.setValue(current.copy(null, -1, current.isResendEnabled(), false, "Enter a valid 6-digit OTP code."));
            return;
        }

        uiState.setValue(current.copy(null, -1, current.isResendEnabled(), true, null));

        firebaseAuthManager.signInWithPhoneCode(verificationId, current.getOtpCode())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        OtpVerificationUiState active = uiState.getValue();
                        if (active != null) {
                            uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, "Signed in user is unavailable."));
                        }
                        return;
                    }

                    // Check Firestore database if user profile exists
                    firestoreRepository.getDocument("users", firebaseUser.getUid())
                            .addOnSuccessListener(documentSnapshot -> {
                                boolean exists = documentSnapshot.exists();
                                boolean isRegistering = registrationName != null && !registrationName.trim().isEmpty();

                                if (isRegistering) {
                                    if (exists) {
                                        // Account already exists: Reject registration, sign out
                                        firebaseAuthManager.signOut();
                                        OtpVerificationUiState active = uiState.getValue();
                                        if (active != null) {
                                            uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, "Account already exists. Please login instead."));
                                        }
                                    } else {
                                        // New user: Save profile and log in
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("name", registrationName);
                                        userData.put("phoneNumber", phoneNumber);
                                        userData.put("createdAt", System.currentTimeMillis());

                                        firestoreRepository.setDocument("users", firebaseUser.getUid(), userData);

                                        OtpVerificationUiState active = uiState.getValue();
                                        if (active != null) {
                                            uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, null));
                                        }
                                        events.setValue(new OtpVerificationEvent.VerificationSuccess());
                                    }
                                } else {
                                    if (exists) {
                                        // Registered user: Proceed to login
                                        OtpVerificationUiState active = uiState.getValue();
                                        if (active != null) {
                                            uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, null));
                                        }
                                        events.setValue(new OtpVerificationEvent.VerificationSuccess());
                                    } else {
                                        // Not registered: Reject login, sign out
                                        firebaseAuthManager.signOut();
                                        OtpVerificationUiState active = uiState.getValue();
                                        if (active != null) {
                                            uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, "User not registered. Please register first."));
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                OtpVerificationUiState active = uiState.getValue();
                                if (active != null) {
                                    uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, FirebaseErrorMapper.mapFirestoreError(e).getMessage()));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    OtpVerificationUiState active = uiState.getValue();
                    if (active != null) {
                        uiState.setValue(active.copy(null, -1, active.isResendEnabled(), false, FirebaseErrorMapper.mapAuthError(e).getMessage()));
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final FirebaseAuthManager firebaseAuthManager;
        private final FirestoreRepository firestoreRepository;
        private final String registrationName;
        private final String phoneNumber;
        private final String initialVerificationId;

        public Factory(FirebaseAuthManager firebaseAuthManager, FirestoreRepository firestoreRepository, String registrationName, String phoneNumber, String initialVerificationId) {
            this.firebaseAuthManager = firebaseAuthManager;
            this.firestoreRepository = firestoreRepository;
            this.registrationName = registrationName;
            this.phoneNumber = phoneNumber;
            this.initialVerificationId = initialVerificationId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(OtpVerificationViewModel.class)) {
                return (T) new OtpVerificationViewModel(firebaseAuthManager, firestoreRepository, registrationName, phoneNumber, initialVerificationId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
