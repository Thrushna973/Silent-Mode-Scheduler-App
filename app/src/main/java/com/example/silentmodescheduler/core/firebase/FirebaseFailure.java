package com.example.silentmodescheduler.core.firebase;

public abstract class FirebaseFailure {
    private final String message;
    private final Throwable cause;

    protected FirebaseFailure(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public static class Authentication extends FirebaseFailure {
        public Authentication(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Firestore extends FirebaseFailure {
        public Firestore(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Network extends FirebaseFailure {
        public Network(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class Unknown extends FirebaseFailure {
        public Unknown(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
