package claimshield;

import java.util.ArrayList;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Represents a policyholder customer in the ClaimShield system.
 * A PolicyHolder is the primary insurance policy owner and can have
 * zero or more associated dependents.
 */
public class PolicyHolder extends Customer {
    private ArrayList<Dependent> dependents;

    /**
     * Constructs a new PolicyHolder with complete customer details, linked
     * insurance card, and total claim amount.
     *
     * @param userId           the unique user identifier (u-7digits)
     * @param username         the username for login
     * @param password         the password for authentication
     * @param fullName         the full name of the policyholder
     * @param email            the email address of the policyholder
     * @param status           the account status (ACTIVE/INACTIVE)
     * @param customerId       the unique customer identifier (c-7digits)
     * @param insuranceCard    the insurance card linked to this customer
     * @param totalClaimAmount the cumulative total claim amount
     */
    public PolicyHolder(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId,
            InsuranceCard insuranceCard,
            double totalClaimAmount) {
        super(userId, username, password, fullName, email, status, customerId, insuranceCard, totalClaimAmount);
        this.dependents = new ArrayList<>();
    }

    /**
     * Constructs a new PolicyHolder without an initial insurance card and with
     * zero total claim amount.
     *
     * @param userId     the unique user identifier (u-7digits)
     * @param username   the username for login
     * @param password   the password for authentication
     * @param fullName   the full name of the policyholder
     * @param email      the email address of the policyholder
     * @param status     the account status (ACTIVE/INACTIVE)
     * @param customerId the unique customer identifier (c-7digits)
     */
    public PolicyHolder(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId) {
        this(userId, username, password, fullName, email, status, customerId, null, 0.0);
    }

    /**
     * Returns a copy of the list of dependents linked to this policyholder.
     *
     * @return an ArrayList containing the dependents
     */
    public ArrayList<Dependent> getDependents() {
        return new ArrayList<>(dependents);
    }

    /**
     * Reports whether this policyholder still covers at least one dependent whose
     * account is currently ACTIVE.
     * <p>
     * Used as a referential guard before a customer account is deactivated, so a
     * policy owner cannot be retired while somebody still depends on the cover.
     * Dependents that are themselves already deactivated do not block the change.
     *
     * @return true if at least one linked dependent is ACTIVE, false otherwise
     */
    public boolean hasActiveDependents() {
        for (Dependent dependent : dependents) {
            if (dependent != null && dependent.getStatus() == UserStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a dependent to this policyholder's family plan.
     *
     * @param dependent the Dependent to add
     */
    public void addDependent(Dependent dependent) {
        if (dependent != null && !dependents.contains(dependent)) {
            dependents.add(dependent);
        }
    }

    /**
     * Removes a dependent from this policyholder's family plan.
     *
     * @param dependent the Dependent to remove
     */
    public void removeDependent(Dependent dependent) {
        dependents.remove(dependent);
    }

    /**
     * Sets the list of dependents for this policyholder.
     *
     * @param dependents the list of dependents to set
     */
    public void setDependents(ArrayList<Dependent> dependents) {
        this.dependents = (dependents != null) ? new ArrayList<>(dependents) : new ArrayList<>();
    }

    @Override
    public String getCustomerType() {
        return "PolicyHolder";
    }

    @Override
    public String getParentPolicyHolderId() {
        return null;
    }

    /**
     * Displays the PolicyHolder dashboard banner and account summary.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=========================================");
        System.out.println("    POLICYHOLDER DASHBOARD - CLAIMSHIELD ");
        System.out.println("=========================================");
        System.out.println("User ID       : " + getUserId());
        System.out.println("Customer ID   : " + getCustomerId());
        System.out.println("Policy Holder : " + getFullName());
        System.out.println("Email         : " + getEmail());
        System.out.println(
                "Card Number   : " + (getInsuranceCard() != null ? getInsuranceCard().getCardNumber() : "None"));
        System.out.println("Dependents    : " + dependents.size());
        System.out.println("Total Claims  : " + getTotalClaimAmount());
        System.out.println("=========================================");
    }

    /**
     * Formats the policyholder record into a 4-column CSV string for customers.txt persistence.
     * Format: customerId,fullName,type,parentId
     *
     * @return the CSV representation of the policyholder for customers.txt
     */
    @Override
    public String toCustomerFileString() {
        return getCustomerId() + ","
                + getFullName() + ","
                + "PolicyHolder,"
                + "null";
    }

    /**
     * Formats the policyholder record for customers.txt persistence.
     * Delegates to toCustomerFileString().
     *
     * @return the CSV representation of the policyholder
     */
    @Override
    public String toFileString() {
        return toCustomerFileString();
    }

    @Override
    public String toString() {
        return "PolicyHolder{customerId=" + getCustomerId()
                + ", name=" + getFullName()
                + ", dependentsCount=" + dependents.size()
                + ", totalClaimAmount=" + getTotalClaimAmount()
                + "}";
    }
}
