package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Exception thrown when a claim date or exam date is invalid or violates
 * chronological business constraints.
 */
public class InvalidClaimDateException extends Exception {

    /**
     * Constructs a new InvalidClaimDateException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidClaimDateException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidClaimDateException with a default message.
     */
    public InvalidClaimDateException() {
        super("Invalid claim date.");
    }

    /**
     * Constructs a new InvalidClaimDateException with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidClaimDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
