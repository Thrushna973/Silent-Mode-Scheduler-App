package com.example.silentmodescheduler.presentation.viewmodel;

import com.example.silentmodescheduler.data.model.TimePeriod;
import java.util.ArrayList;
import java.util.List;

public class MainUiState {
    private final String userName;
    private final List<TimePeriod> timePeriods;
    private final boolean isLoading;
    private final String error;

    public MainUiState() {
        this.userName = "User";
        this.timePeriods = new ArrayList<>();
        this.isLoading = false;
        this.error = null;
    }

    public MainUiState(String userName, List<TimePeriod> timePeriods, boolean isLoading, String error) {
        this.userName = userName;
        this.timePeriods = timePeriods;
        this.isLoading = isLoading;
        this.error = error;
    }

    public String getUserName() {
        return userName;
    }

    public List<TimePeriod> getTimePeriods() {
        return timePeriods;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getError() {
        return error;
    }

    public MainUiState copy(String userName, List<TimePeriod> timePeriods, boolean isLoading, String error) {
        return new MainUiState(
                userName != null ? userName : this.userName,
                timePeriods != null ? timePeriods : this.timePeriods,
                isLoading,
                error
        );
    }
}
