package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Abstract base class for all checked business-rule exceptions raised by the
 * ClaimShield domain and persistence layers.
 * <p>
 * Introducing this common supertype allows the generic repository contract
 * {@link Manageable} to declare a single, precise
 * {@code throws ClaimShieldException} clause instead of the overly broad
 * {@code throws Exception}. Each concrete repository is then free to narrow
 * that clause to the specific business exception it can actually raise (or to
 * omit it entirely when it raises none), which keeps calling code explicit
 * about the failure modes it must handle.
 *
 * @see InvalidClaimDateException
 * @see InvalidStatusTransitionException
 */
public abstract class ClaimShieldException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ClaimShieldException with the specified detail message.
     *
     * @param message the detail message
     */
    protected ClaimShieldException(String message) {
        super(message);
    }

    /**
     * Constructs a new ClaimShieldException with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    protected ClaimShieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
