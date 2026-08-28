package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.time.LocalDateTime;
import java.util.List;

/**
 * Specialized interface extending Manageable for claim-specific operations
 * and query filtering.
 */
public interface ClaimManageable extends Manageable<Claim> {

    /**
     * Filters claims by their current processing status.
     *
     * @param status the ClaimStatus to filter by
     * @return a List of matching claims
     */
    List<Claim> filterByStatus(ClaimStatus status);

    /**
     * Filters claims within a specified date range (inclusive).
     *
     * @param start the start date-time (inclusive)
     * @param end   the end date-time (inclusive)
     * @return a List of claims within the date range
     */
    List<Claim> filterByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Filters claims belonging to a PolicyHolder and all of their associated
     * dependents.
     *
     * @param policyHolderId the customer ID of the policyholder
     * @return a List of claims for the entire policyholder family
     */
    List<Claim> filterByPolicyHolderFamily(String policyHolderId);
}
