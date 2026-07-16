package com.example.silentmodescheduler.core.firebase;

public interface FirebaseResult<T> {
    class Success<T> implements FirebaseResult<T> {
        private final T data;
        public Success(T data) {
            this.data = data;
        }
        public T getData() {
            return data;
        }
    }

    class Failure<T> implements FirebaseResult<T> {
        private final FirebaseFailure error;
        public Failure(FirebaseFailure error) {
            this.error = error;
        }
        public FirebaseFailure getError() {
            return error;
        }
    }
}
