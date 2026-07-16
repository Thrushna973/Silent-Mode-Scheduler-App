package com.example.silentmodescheduler.presentation.viewmodel;

public class TimePeriodUiState {
    private final String name;
    private final String startTime;
    private final String endTime;
    private final String nameError;
    private final String startTimeError;
    private final String endTimeError;
    private final boolean isLoading;
    private final boolean isSaveSuccess;
    private final String generalError;
    private final boolean isEditMode;

    public TimePeriodUiState(boolean isEditMode) {
        this.name = "";
        this.startTime = "";
        this.endTime = "";
        this.nameError = null;
        this.startTimeError = null;
        this.endTimeError = null;
        this.isLoading = false;
        this.isSaveSuccess = false;
        this.generalError = null;
        this.isEditMode = isEditMode;
    }

    public TimePeriodUiState(String name, String startTime, String endTime, String nameError, String startTimeError, String endTimeError, boolean isLoading, boolean isSaveSuccess, String generalError, boolean isEditMode) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.nameError = nameError;
        this.startTimeError = startTimeError;
        this.endTimeError = endTimeError;
        this.isLoading = isLoading;
        this.isSaveSuccess = isSaveSuccess;
        this.generalError = generalError;
        this.isEditMode = isEditMode;
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getNameError() {
        return nameError;
    }

    public String getStartTimeError() {
        return startTimeError;
    }

    public String getEndTimeError() {
        return endTimeError;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isSaveSuccess() {
        return isSaveSuccess;
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public TimePeriodUiState copy(String name, String startTime, String endTime, String nameError, String startTimeError, String endTimeError, boolean isLoading, boolean isSaveSuccess, String generalError) {
        return new TimePeriodUiState(
                name != null ? name : this.name,
                startTime != null ? startTime : this.startTime,
                endTime != null ? endTime : this.endTime,
                nameError,
                startTimeError,
                endTimeError,
                isLoading,
                isSaveSuccess,
                generalError,
                this.isEditMode
        );
    }
}
