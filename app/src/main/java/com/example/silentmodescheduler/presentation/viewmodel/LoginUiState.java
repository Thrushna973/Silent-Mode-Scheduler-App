package com.example.silentmodescheduler.presentation.viewmodel;

public class LoginUiState {
    private final String phoneNumber;
    private final String phoneNumberError;
    private final String generalError;
    private final boolean isLoading;

    public LoginUiState() {
        this.phoneNumber = "";
        this.phoneNumberError = null;
        this.generalError = null;
        this.isLoading = false;
    }

    public LoginUiState(String phoneNumber, String phoneNumberError, String generalError, boolean isLoading) {
        this.phoneNumber = phoneNumber;
        this.phoneNumberError = phoneNumberError;
        this.generalError = generalError;
        this.isLoading = isLoading;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhoneNumberError() {
        return phoneNumberError;
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public LoginUiState copy(String phoneNumber, String phoneNumberError, String generalError, boolean isLoading) {
        return new LoginUiState(
                phoneNumber != null ? phoneNumber : this.phoneNumber,
                phoneNumberError,
                generalError,
                isLoading
        );
    }
}
