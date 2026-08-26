package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

/**
 * Represents a dependent customer in the ClaimShield system.
 * A Dependent is covered under a PolicyHolder's insurance plan and
 * references the parent PolicyHolder via customerId.
 */
public class Dependent extends Customer {
    private String parentPolicyHolderId;

    /**
     * Constructs a new Dependent with complete customer details, linked
     * insurance card, total claim amount, and parent policyholder reference.
     *
     * @param userId               the unique user identifier (u-7digits)
     * @param username             the username for login
     * @param password             the password for authentication
     * @param fullName             the full name of the dependent
     * @param email                the email address of the dependent
     * @param status               the account status (ACTIVE/INACTIVE)
     * @param customerId           the unique customer identifier (c-7digits)
     * @param insuranceCard        the insurance card linked to this customer
     * @param totalClaimAmount     the cumulative total claim amount
     * @param parentPolicyHolderId the customer ID of the covering PolicyHolder
     */
    public Dependent(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId,
            InsuranceCard insuranceCard,
            double totalClaimAmount,
            String parentPolicyHolderId) {
        super(userId, username, password, fullName, email, status, customerId, insuranceCard, totalClaimAmount);
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    /**
     * Constructs a new Dependent without an initial insurance card and with
     * zero total claim amount.
     *
     * @param userId               the unique user identifier (u-7digits)
     * @param username             the username for login
     * @param password             the password for authentication
     * @param fullName             the full name of the dependent
     * @param email                the email address of the dependent
     * @param status               the account status (ACTIVE/INACTIVE)
     * @param customerId           the unique customer identifier (c-7digits)
     * @param parentPolicyHolderId the customer ID of the covering PolicyHolder
     */
    public Dependent(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId,
            String parentPolicyHolderId) {
        this(userId, username, password, fullName, email, status, customerId, null, 0.0, parentPolicyHolderId);
    }

    @Override
    public String getParentPolicyHolderId() {
        return parentPolicyHolderId;
    }

    /**
     * Sets the parent policyholder ID for this dependent.
     *
     * @param parentPolicyHolderId the customer ID of the covering PolicyHolder
     */
    public void setParentPolicyHolderId(String parentPolicyHolderId) {
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    @Override
    public String getCustomerType() {
        return "Dependent";
    }

    /**
     * Displays the Dependent dashboard banner and account summary.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=========================================");
        System.out.println("      DEPENDENT DASHBOARD - CLAIMSHIELD  ");
        System.out.println("=========================================");
        System.out.println("User ID       : " + getUserId());
        System.out.println("Customer ID   : " + getCustomerId());
        System.out.println("Dependent     : " + getFullName());
        System.out.println("Email         : " + getEmail());
        System.out.println("Policy Holder : " + (parentPolicyHolderId != null ? parentPolicyHolderId : "None"));
        System.out.println(
                "Card Number   : " + (getInsuranceCard() != null ? getInsuranceCard().getCardNumber() : "None"));
        System.out.println("Total Claims  : " + getTotalClaimAmount());
        System.out.println("=========================================");
    }

    /**
     * Formats the dependent record into a 4-column CSV string for customers.txt
     * persistence.
     * Format: customerId,fullName,type,parentId
     *
     * @return the CSV representation of the dependent for customers.txt
     */
    @Override
    public String toCustomerFileString() {
        return getCustomerId() + ","
                + getFullName() + ","
                + "Dependent,"
                + parentPolicyHolderId;
    }

    /**
     * Formats the dependent record for customers.txt persistence.
     * Delegates to toCustomerFileString().
     *
     * @return the CSV representation of the dependent
     */
    @Override
    public String toFileString() {
        return toCustomerFileString();
    }

    @Override
    public String toString() {
        return "Dependent{customerId=" + getCustomerId()
                + ", name=" + getFullName()
                + ", parentPolicyHolderId=" + parentPolicyHolderId
                + ", totalClaimAmount=" + getTotalClaimAmount()
                + "}";
    }
}
