package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Exception thrown when an invalid claim status transition is attempted
 * (e.g., transitioning backward or skipping required stages).
 */
public class InvalidStatusTransitionException extends Exception {

    /**
     * Constructs a new InvalidStatusTransitionException with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidStatusTransitionException with a default message.
     */
    public InvalidStatusTransitionException() {
        super("Invalid status transition.");
    }

    /**
     * Constructs a new InvalidStatusTransitionException with the specified detail
     * message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
