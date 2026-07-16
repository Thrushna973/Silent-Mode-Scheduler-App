package com.example.silentmodescheduler.presentation.viewmodel;

public abstract class RegisterEvent {
    public static class OtpSent extends RegisterEvent {
        private final String name;
        private final String phoneNumber;
        private final String verificationId;

        public OtpSent(String name, String phoneNumber, String verificationId) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.verificationId = verificationId;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getVerificationId() {
            return verificationId;
        }
    }
}
