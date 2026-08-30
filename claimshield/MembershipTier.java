package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
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

    /**
     * Computes the membership tier based on the total claim amount spent.
     * Tier thresholds:
     * - PLATINUM: total >= 5,000,000
     * - GOLD:     total >= 2,000,000 and < 5,000,000
     * - SILVER:   total < 2,000,000
     *
     * @param totalClaimAmount the cumulative total approved claim amount
     * @return the corresponding MembershipTier
     */
    public static MembershipTier computeTier(double totalClaimAmount) {
        if (totalClaimAmount >= 5000000.0) {
            return PLATINUM;
        } else if (totalClaimAmount >= 2000000.0) {
            return GOLD;
        } else {
            return SILVER;
        }
    }
}
