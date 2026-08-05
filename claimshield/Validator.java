package claimshield;
/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */
public class Validator {
    public static boolean isValidCustomerId(String id) {
    if (id == null) return false;
    return id.matches("c-\\d{7}");
    }

    public static boolean isValidClaimId(String id) {
    if (id == null) return false;
    return id.matches("f-\\d{10}");
    }

    public static boolean isValidCardNumber(String cardNumber) {
    if (cardNumber == null) return false;
    return cardNumber.matches("\\d{10}");
    }
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }
    public static boolean isValidDocumentName(String fileName, String claimId, String cardNumber) {
    if (fileName == null) return false;
    String expectedPrefix = claimId + "_" + cardNumber + "_";
    return fileName.startsWith(expectedPrefix) && fileName.endsWith(".pdf");
    }
}
