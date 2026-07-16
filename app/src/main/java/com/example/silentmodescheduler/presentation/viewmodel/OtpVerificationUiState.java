package com.example.silentmodescheduler.presentation.viewmodel;

public class OtpVerificationUiState {
    private final String otpCode;
    private final int countdownSeconds;
    private final boolean isResendEnabled;
    private final boolean isLoading;
    private final String error;

    public OtpVerificationUiState() {
        this.otpCode = "";
        this.countdownSeconds = 30;
        this.isResendEnabled = false;
        this.isLoading = false;
        this.error = null;
    }

    public OtpVerificationUiState(String otpCode, int countdownSeconds, boolean isResendEnabled, boolean isLoading, String error) {
        this.otpCode = otpCode;
        this.countdownSeconds = countdownSeconds;
        this.isResendEnabled = isResendEnabled;
        this.isLoading = isLoading;
        this.error = error;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public boolean isResendEnabled() {
        return isResendEnabled;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getError() {
        return error;
    }

    public OtpVerificationUiState copy(String otpCode, int countdownSeconds, boolean isResendEnabled, boolean isLoading, String error) {
        return new OtpVerificationUiState(
                otpCode != null ? otpCode : this.otpCode,
                countdownSeconds != -1 ? countdownSeconds : this.countdownSeconds,
                isResendEnabled,
                isLoading,
                error
        );
    }
}
