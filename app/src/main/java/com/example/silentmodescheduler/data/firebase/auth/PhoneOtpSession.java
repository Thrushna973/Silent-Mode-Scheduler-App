package com.example.silentmodescheduler.data.firebase.auth;

import com.google.firebase.auth.PhoneAuthProvider;

public class PhoneOtpSession {
    private final String verificationId;
    private final PhoneAuthProvider.ForceResendingToken resendToken;

    public PhoneOtpSession(String verificationId, PhoneAuthProvider.ForceResendingToken resendToken) {
        this.verificationId = verificationId;
        this.resendToken = resendToken;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public PhoneAuthProvider.ForceResendingToken getResendToken() {
        return resendToken;
    }
}
