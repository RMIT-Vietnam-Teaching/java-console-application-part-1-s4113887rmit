package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.util.regex.Pattern;

/**
 * Provides static validation methods for checking the format and
 * business rules of IDs, amounts, dates, and other fields used
 * throughout the ClaimShield system.
 */
public class Validator {

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
     * Checks whether a customer is a PolicyHolder.
     *
     * @param customer the customer to check
     * @return true if the customer is a PolicyHolder
     */
    public static boolean isPolicyHolder(Customer customer) {
        return customer != null
                && customer.getCustomerType().equals("PolicyHolder");
    }
}