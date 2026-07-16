package com.example.silentmodescheduler.data.firebase.auth;

import android.app.Activity;
import com.example.silentmodescheduler.core.firebase.FirebaseErrorMapper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.TimeUnit;

public class FirebaseAuthManager {
    private final FirebaseAuth firebaseAuth;
    private static final long PHONE_AUTH_TIMEOUT_SECONDS = 60L;

    public FirebaseAuthManager() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseAuthManager(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    public interface PhoneVerificationCallback {
        void onCodeSent(PhoneOtpSession session);
        void onVerificationCompleted(PhoneAuthCredential credential);
        void onVerificationFailed(String errorMessage);
    }

    public void sendPhoneVerificationCode(
            Activity activity,
            String phoneNumber,
            PhoneVerificationCallback callback
    ) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        callback.onVerificationCompleted(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        callback.onVerificationFailed(FirebaseErrorMapper.mapAuthError(e).getMessage());
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        callback.onCodeSent(new PhoneOtpSession(verificationId, token));
                    }
                };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(PHONE_AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();

        try {
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            callback.onVerificationFailed(FirebaseErrorMapper.mapAuthError(e).getMessage());
        }
    }

    public Task<AuthResult> signInWithCredential(PhoneAuthCredential credential) {
        return firebaseAuth.signInWithCredential(credential);
    }

    public Task<AuthResult> signInWithPhoneCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        return firebaseAuth.signInWithCredential(credential);
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}
