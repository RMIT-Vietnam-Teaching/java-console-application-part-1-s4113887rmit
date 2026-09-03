package claimshield;

import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Specialized interface extending {@link Manageable} for customer-specific
 * search filters. Implemented by {@link CustomerRepository}.
 * <p>
 * The filters declared here operate polymorphically over the Customer
 * hierarchy, returning {@link PolicyHolder} and {@link Dependent} instances
 * through the shared {@link Customer} supertype.
 */
public interface CustomerManageable extends Manageable<Customer> {

    /**
     * Filters customers by their concrete position in the Customer hierarchy.
     *
     * @param customerType the customer type to match, case-insensitive; accepted
     *                     values are "PolicyHolder" and "Dependent"
     * @return a List of matching customers, empty if the type is unknown
     */
    List<Customer> filterByType(String customerType);

    /**
     * Filters customers to only those covered as dependents under a given
     * PolicyHolder.
     *
     * @param policyHolderId the customer ID of the covering PolicyHolder
     * @return a List of dependents linked to that PolicyHolder
     */
    List<Customer> filterByParentPolicyHolder(String policyHolderId);

    /**
     * Performs a case-insensitive partial-match search on customer full names.
     *
     * @param keyword the name fragment to search for
     * @return a List of customers whose full name contains the keyword
     */
    List<Customer> searchByName(String keyword);
}
