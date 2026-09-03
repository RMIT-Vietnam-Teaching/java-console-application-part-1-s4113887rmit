package claimshield;

import java.util.regex.Pattern;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Provides static validation methods for checking the format and
 * business rules of IDs, amounts, dates, and other fields used
 * throughout the ClaimShield system.
 */
public class Validator {

    // ------------------------------------------------------------------
    // Free-text field length bounds.
    // These bounds keep user-supplied strings within a sane range so that
    // neither an empty field nor an unbounded one can reach the data files.
    // ------------------------------------------------------------------

    /** Minimum accepted username length. */
    public static final int USERNAME_MIN_LENGTH = 3;
    /** Maximum accepted username length. */
    public static final int USERNAME_MAX_LENGTH = 50;
    /** Minimum accepted password length. */
    public static final int PASSWORD_MIN_LENGTH = 4;
    /** Maximum accepted password length. */
    public static final int PASSWORD_MAX_LENGTH = 64;
    /** Minimum accepted full name length. */
    public static final int NAME_MIN_LENGTH = 2;
    /** Maximum accepted full name length. */
    public static final int NAME_MAX_LENGTH = 100;
    /** Minimum accepted email length. */
    public static final int EMAIL_MIN_LENGTH = 5;
    /** Maximum accepted email length. */
    public static final int EMAIL_MAX_LENGTH = 100;

    /**
     * Checks if a customer ID matches the format c-XXXXXXX (7 digits).
     *
     * @param id the ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCustomerId(String id) {
        if (id == null) {
            return false;
        }

        return id.matches("c-\\d{7}");
    }

    /**
     * Checks if a claim ID matches the format f-XXXXXXXXXX (10 digits).
     *
     * @param id the ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidClaimId(String id) {
        if (id == null) {
            return false;
        }

        return id.matches("f-\\d{10}");
    }

    /**
     * Checks if a card number consists of exactly 10 digits.
     *
     * @param cardNumber the card number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        return cardNumber.matches("\\d{10}");
    }

    /**
     * Checks if an amount is strictly greater than zero.
     *
     * @param amount the amount to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    /**
     * Checks if a document name follows:
     * ClaimId_CardNumber_DocumentName.pdf.
     *
     * @param fileName the document name to validate
     * @param claimId the claim ID that should prefix the name
     * @param cardNumber the card number that should follow the claim ID
     * @return true if valid, false otherwise
     */
    public static boolean isValidDocumentName(
            String fileName,
            String claimId,
            String cardNumber
    ) {
        if (fileName == null || claimId == null || cardNumber == null) {
            return false;
        }

        String pattern = Pattern.quote(
                claimId + "_" + cardNumber + "_"
        ) + ".+\\.pdf";

        return fileName.matches(pattern);
    }

    /**
     * Checks if a customer type is either PolicyHolder or Dependent.
     *
     * @param type the customer type to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCustomerType(String type) {
        if (type == null) {
            return false;
        }

        return type.equals("PolicyHolder") || type.equals("Dependent");
    }

    /**
     * Checks if a status string is a valid claim status (New, Processing, or Done).
     *
     * @param status the status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStatus(String status) {
        return ClaimStatus.fromString(status) != null;
    }

    /**
     * Checks if a ClaimStatus enum is valid (non-null).
     *
     * @param status the status enum to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStatus(ClaimStatus status) {
        return status != null;
    }

    /**
     * Checks if a user ID matches the format u-XXXXXXX (7 digits).
     *
     * @param id the user ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUserId(String id) {
        if (id == null) {
            return false;
        }

        return id.matches("u-\\d{7}");
    }

    /**
     * Checks whether a customer is a PolicyHolder.
     *
     * @param customer the customer to check
     * @return true if the customer is a PolicyHolder
     */
    public static boolean isPolicyHolder(Customer customer) {
        return customer != null
                && customer.getCustomerType().equals("PolicyHolder");
    }

    // ------------------------------------------------------------------
    // String length validation
    // ------------------------------------------------------------------

    /**
     * Checks that a trimmed string's length falls within inclusive bounds.
     *
     * @param value the string to measure
     * @param min   the minimum accepted length (inclusive)
     * @param max   the maximum accepted length (inclusive)
     * @return true if value is non-null and its trimmed length is within bounds
     */
    public static boolean isValidLength(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    /**
     * Checks that a username is within the accepted length range.
     *
     * @param username the username to validate
     * @return true if the username length is accepted
     */
    public static boolean isValidUsername(String username) {
        return isValidLength(username, USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH);
    }

    /**
     * Checks that a password is within the accepted length range.
     *
     * @param password the password to validate
     * @return true if the password length is accepted
     */
    public static boolean isValidPassword(String password) {
        return isValidLength(password, PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH);
    }

    /**
     * Checks that a full name is within the accepted length range.
     *
     * @param fullName the full name to validate
     * @return true if the full name length is accepted
     */
    public static boolean isValidFullName(String fullName) {
        return isValidLength(fullName, NAME_MIN_LENGTH, NAME_MAX_LENGTH);
    }

    /**
     * Checks that an email address is within the accepted length range and
     * contains the '@' character separating the local part from the domain.
     *
     * @param email the email address to validate
     * @return true if the email address is accepted
     */
    public static boolean isValidEmail(String email) {
        return isValidLength(email, EMAIL_MIN_LENGTH, EMAIL_MAX_LENGTH)
                && email.contains("@");
    }
}