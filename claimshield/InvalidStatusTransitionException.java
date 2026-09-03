package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Exception thrown when an invalid claim status transition is attempted
 * (e.g., transitioning backward or skipping required stages).
 * <p>
 * Raised by {@link ClaimRepository#updateClaimStatus} when a claim already
 * marked DONE is modified, or when a transition attempts to move more than one
 * step forward through the NEW -&gt; PROCESSING -&gt; DONE workflow.
 */
public class InvalidStatusTransitionException extends ClaimShieldException {
    private static final long serialVersionUID = 1L;

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
