package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Defines the status states of a claim in the ClaimShield system.
 */
public enum ClaimStatus {
    NEW,
    PROCESSING,
    DONE;

    /**
     * Parses a string representation into a ClaimStatus, handling both
     * uppercase enum names ("NEW", "PROCESSING", "DONE") and mixed-case
     * values ("New", "Processing", "Done").
     *
     * @param value the string representation of the claim status
     * @return the corresponding ClaimStatus, or null if invalid
     */
    public static ClaimStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        switch (clean) {
            case "NEW":
                return NEW;
            case "PROCESSING":
                return PROCESSING;
            case "DONE":
                return DONE;
            default:
                return null;
        }
    }
}
