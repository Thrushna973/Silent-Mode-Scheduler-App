package com.example.silentmodescheduler.presentation.viewmodel;

public abstract class LoginEvent {
    public static class OtpSent extends LoginEvent {
        private final String phoneNumber;
        private final String verificationId;

        public OtpSent(String phoneNumber, String verificationId) {
            this.phoneNumber = phoneNumber;
            this.verificationId = verificationId;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getVerificationId() {
            return verificationId;
        }
    }
}
