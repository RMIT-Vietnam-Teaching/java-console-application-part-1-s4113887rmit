package claimshield;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Specialized interface extending {@link Manageable} for insurance-card
 * specific search filters. Implemented by {@link CardRepository}.
 * <p>
 * Card lookups are keyed by the 10-digit card number (via
 * {@link Manageable#getById}), while the filters below resolve cards through
 * the customer relationships recorded on them.
 */
public interface CardManageable extends Manageable<InsuranceCard> {

    /**
     * Finds the insurance card issued to a specific card holder.
     *
     * @param cardHolderId the customer ID of the card holder
     * @return the matching InsuranceCard, or null if none is issued
     */
    InsuranceCard getByCardHolderId(String cardHolderId);

    /**
     * Finds all insurance cards paid for by a specific policy owner, which for a
     * family plan includes the cards issued to that owner's dependents.
     *
     * @param policyOwnerId the customer ID of the policy owner
     * @return a List of insurance cards funded by that policy owner
     */
    List<InsuranceCard> getByPolicyOwnerId(String policyOwnerId);

    /**
     * Finds all insurance cards that expire strictly before a given instant,
     * useful for renewal campaigns and expiry reporting.
     *
     * @param deadline the exclusive upper bound on the expiration date
     * @return a List of insurance cards expiring before the deadline
     */
    List<InsuranceCard> filterExpiringBefore(LocalDateTime deadline);
}
