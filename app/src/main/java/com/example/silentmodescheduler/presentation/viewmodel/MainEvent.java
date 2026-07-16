package com.example.silentmodescheduler.presentation.viewmodel;

public abstract class MainEvent {
    public static class LoggedOut extends MainEvent {}

    public static class ShowSnackbar extends MainEvent {
        private final String message;

        public ShowSnackbar(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
