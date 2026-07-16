package com.example.silentmodescheduler.presentation.viewmodel;

public class RegisterUiState {
    private final String name;
    private final String phoneNumber;
    private final String nameError;
    private final String phoneNumberError;
    private final String generalError;
    private final boolean isLoading;

    public RegisterUiState() {
        this.name = "";
        this.phoneNumber = "";
        this.nameError = null;
        this.phoneNumberError = null;
        this.generalError = null;
        this.isLoading = false;
    }

    public RegisterUiState(String name, String phoneNumber, String nameError, String phoneNumberError, String generalError, boolean isLoading) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nameError = nameError;
        this.phoneNumberError = phoneNumberError;
        this.generalError = generalError;
        this.isLoading = isLoading;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNameError() {
        return nameError;
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

    public RegisterUiState copy(String name, String phoneNumber, String nameError, String phoneNumberError, String generalError, boolean isLoading) {
        return new RegisterUiState(
                name != null ? name : this.name,
                phoneNumber != null ? phoneNumber : this.phoneNumber,
                nameError,
                phoneNumberError,
                generalError,
                isLoading
        );
    }
}
