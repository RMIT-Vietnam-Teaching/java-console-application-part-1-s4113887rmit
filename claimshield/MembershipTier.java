package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Defines the membership tiers available for policyholders/customers in the
 * ClaimShield system.
 */
public enum MembershipTier {
    STANDARD(0.00),
    SILVER(0.05),
    GOLD(0.10),
    PLATINUM(0.15);

    /** Standard base co-pay rate across all customers before tier discount (30%). */
    public static final double BASE_COPAY_RATE = 0.30;

    private final double discountRate;

    MembershipTier(double discountRate) {
        this.discountRate = discountRate;
    }

    /**
     * Gets the co-pay discount rate associated with this membership tier.
     * - PLATINUM: 0.15 (15%)
     * - GOLD:     0.10 (10%)
     * - SILVER:   0.05 (5%)
     * - STANDARD: 0.00 (0%)
     *
     * @return the discount rate as a decimal
     */
    public double getDiscountRate() {
        return discountRate;
    }

    /**
     * Calculates the effective co-pay rate after applying this tier's discount.
     * Formula: Effective Co-Pay Rate = BASE_COPAY_RATE * (1 - discountRate)
     * - STANDARD: 30.0%
     * - SILVER:   28.5%
     * - GOLD:     27.0%
     * - PLATINUM: 25.5%
     *
     * @return effective co-pay rate as a decimal
     */
    public double getEffectiveCopayRate() {
        return BASE_COPAY_RATE * (1.0 - discountRate);
    }

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
     * - SILVER:   total >= 500,000 and < 2,000,000
     * - STANDARD: total < 500,000
     *
     * @param totalClaimAmount the cumulative total approved claim amount
     * @return the corresponding MembershipTier
     */
    public static MembershipTier computeTier(double totalClaimAmount) {
        if (totalClaimAmount >= 5000000.0) {
            return PLATINUM;
        } else if (totalClaimAmount >= 2000000.0) {
            return GOLD;
        } else if (totalClaimAmount >= 500000.0) {
            return SILVER;
        } else {
            return STANDARD;
        }
    }
}
