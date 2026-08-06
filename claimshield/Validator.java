package claimshield;
import java.util.regex.Pattern;

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
    String pattern = Pattern.quote(claimId + "_" + cardNumber + "_") + ".+\\.pdf";
    return fileName.matches(pattern);
    }
    public static boolean isValidCustomerType(String type) {
    if (type == null) return false;
    return type.equals("PolicyHolder") || type.equals("Dependent");
    }

    public static boolean isValidStatus(String status) {
    if (status == null) return false;
    return status.equals("New") || status.equals("Processing") || status.equals("Done");
    }

    public static boolean isPolicyHolder(Customer customer) {
    return customer != null && customer.getCustomerType().equals("PolicyHolder");
    }
}