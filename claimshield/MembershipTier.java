package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Defines the membership tiers available for policyholders/customers in the
 * ClaimShield system.
 */
public enum MembershipTier {
    SILVER,
    GOLD,
    PLATINUM;

    /**
     * Parses a string representation into a MembershipTier (case-insensitive).
     *
     * @param value the string representation of the membership tier
     * @return the corresponding MembershipTier, or null if invalid
     */
    public static MembershipTier fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return MembershipTier.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
