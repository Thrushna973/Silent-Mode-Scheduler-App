package com.example.silentmodescheduler.core.firebase;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class FirebaseErrorMapper {
    public static FirebaseFailure mapAuthError(Throwable error) {
        if (error instanceof FirebaseNetworkException) {
            return new FirebaseFailure.Network("Network connection failed. Please try again.", error);
        } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return new FirebaseFailure.Authentication("Enter a valid phone number and try again.", error);
        } else if (error instanceof FirebaseTooManyRequestsException) {
            return new FirebaseFailure.Authentication("Too many attempts. Please wait before trying again.", error);
        } else if (error instanceof FirebaseAuthException) {
            return new FirebaseFailure.Authentication(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Authentication failed.", error);
        } else {
            return new FirebaseFailure.Unknown(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Unexpected authentication error.", error);
        }
    }

    public static FirebaseFailure mapFirestoreError(Throwable error) {
        if (error instanceof FirebaseNetworkException) {
            return new FirebaseFailure.Network("Network connection failed. Please try again.", error);
        } else if (error instanceof FirebaseFirestoreException) {
            return new FirebaseFailure.Firestore(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Database operation failed.", error);
        } else {
            return new FirebaseFailure.Unknown(error.getLocalizedMessage() != null ? error.getLocalizedMessage() : "Unexpected database error.", error);
        }
    }
}
