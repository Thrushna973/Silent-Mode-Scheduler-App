package com.example.silentmodescheduler.presentation.auth;

import java.util.regex.Pattern;

public class RegistrationValidator {
    private static final Pattern indianMobileNumberPattern = Pattern.compile("^[6-9]\\d{9}$");

    public static class RegistrationValidationResult {
        public final String nameError;
        public final String phoneNumberError;
        public final String normalizedName;
        public final String normalizedPhoneNumber;
        public final boolean isValid;

        public RegistrationValidationResult(String nameError, String phoneNumberError, String normalizedName, String normalizedPhoneNumber, boolean isValid) {
            this.nameError = nameError;
            this.phoneNumberError = phoneNumberError;
            this.normalizedName = normalizedName;
            this.normalizedPhoneNumber = normalizedPhoneNumber;
            this.isValid = isValid;
        }
    }

    public static class PhoneValidationResult {
        public final String error;
        public final String normalizedPhoneNumber;
        public final boolean isValid;

        public PhoneValidationResult(String error, String normalizedPhoneNumber, boolean isValid) {
            this.error = error;
            this.normalizedPhoneNumber = normalizedPhoneNumber;
            this.isValid = isValid;
        }
    }

    public static RegistrationValidationResult validate(String name, String phoneNumber) {
        String trimmedName = name != null ? name.trim() : "";
        PhoneValidationResult phoneValidation = validatePhoneNumber(phoneNumber);

        String nameError = null;
        if (trimmedName.isEmpty()) {
            nameError = "Name is required.";
        } else if (trimmedName.length() < 2) {
            nameError = "Enter a valid name.";
        }

        boolean isValid = nameError == null && phoneValidation.isValid;

        return new RegistrationValidationResult(
                nameError,
                phoneValidation.error,
                trimmedName,
                phoneValidation.normalizedPhoneNumber,
                isValid
        );
    }

    public static PhoneValidationResult validatePhoneNumber(String phoneNumber) {
        String trimmedPhone = phoneNumber != null ? phoneNumber.trim() : "";
        String normalizedPhoneNumber = normalizeIndianPhoneNumber(trimmedPhone);

        String phoneError = null;
        if (trimmedPhone.isEmpty()) {
            phoneError = "Phone number is required.";
        } else if (normalizedPhoneNumber == null) {
            phoneError = "Enter a valid 10-digit Indian mobile number.";
        }

        return new PhoneValidationResult(
                phoneError,
                normalizedPhoneNumber,
                phoneError == null && normalizedPhoneNumber != null
        );
    }

    private static String normalizeIndianPhoneNumber(String phoneNumber) {
        StringBuilder digitsOnly = new StringBuilder();
        for (char c : phoneNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                digitsOnly.append(c);
            }
        }

        String digitsStr = digitsOnly.toString();
        String mobileNumber = null;

        if (digitsStr.length() == 10) {
            mobileNumber = digitsStr;
        } else if (digitsStr.length() == 12 && digitsStr.startsWith("91")) {
            mobileNumber = digitsStr.substring(2);
        } else {
            return null;
        }

        if (indianMobileNumberPattern.matcher(mobileNumber).matches()) {
            return "+91" + mobileNumber;
        } else {
            return null;
        }
    }
}
