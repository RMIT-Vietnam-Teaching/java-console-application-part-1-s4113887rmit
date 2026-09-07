package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Exception thrown when a document file name does not rigidly match the required
 * naming format: ClaimId_CardNumber_DocName.pdf.
 */
public class InvalidDocumentNameException extends ClaimShieldException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new InvalidDocumentNameException with the specified detail message.
     *
     * @param message detail explanation of the validation failure
     */
    public InvalidDocumentNameException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidDocumentNameException with a default error message.
     */
    public InvalidDocumentNameException() {
        super("Invalid document file name format. Expected: ClaimId_CardNumber_DocName.pdf");
    }

    /**
     * Constructs a new InvalidDocumentNameException with the specified detail message
     * and cause.
     *
     * @param message detail explanation of the validation failure
     * @param cause   underlying cause
     */
    public InvalidDocumentNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
