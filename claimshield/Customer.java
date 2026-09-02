package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Abstract base class representing a customer in the ClaimShield system.
 * A Customer is a User with customer-specific attributes including a
 * customer ID, an insurance card reference, and a total claim amount.
 */
public abstract class Customer extends User {
    private String customerId;
    private InsuranceCard insuranceCard;
    private double totalClaimAmount;

    /**
     * Constructs a new Customer with full details including insurance card
     * and total claim amount.
     *
     * @param userId           the unique user identifier (u-7digits)
     * @param username         the username for login
     * @param password         the password for authentication
     * @param fullName         the full name of the customer
     * @param email            the email address of the customer
     * @param status           the account status (ACTIVE/INACTIVE)
     * @param customerId       the unique customer identifier (c-7digits)
     * @param insuranceCard    the insurance card linked to this customer
     * @param totalClaimAmount the cumulative total claim amount
     */
    public Customer(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId,
            InsuranceCard insuranceCard,
            double totalClaimAmount
    ) {
        super(userId, username, password, fullName, email, UserRole.CUSTOMER, status);
        this.customerId = customerId;
        this.insuranceCard = insuranceCard;
        this.totalClaimAmount = totalClaimAmount;
    }

    /**
     * Constructs a new Customer without an initial insurance card and with
     * zero total claim amount.
     *
     * @param userId     the unique user identifier (u-7digits)
     * @param username   the username for login
     * @param password   the password for authentication
     * @param fullName   the full name of the customer
     * @param email      the email address of the customer
     * @param status     the account status (ACTIVE/INACTIVE)
     * @param customerId the unique customer identifier (c-7digits)
     */
    public Customer(
            String userId,
            String username,
            String password,
            String fullName,
            String email,
            UserStatus status,
            String customerId
    ) {
        this(userId, username, password, fullName, email, status, customerId, null, 0.0);
    }

    /**
     * Gets the unique customer identifier (c-7digits).
     *
     * @return the customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Gets the customer identifier, providing compatibility with getId().
     *
     * @return the customer ID
     */
    public String getId() {
        return customerId;
    }

    /**
     * Gets the insurance card linked to this customer.
     *
     * @return the InsuranceCard, or null if no card is linked
     */
    public InsuranceCard getInsuranceCard() {
        return insuranceCard;
    }

    /**
     * Gets the total claim amount requested or paid for this customer.
     *
     * @return the total claim amount
     */
    public double getTotalClaimAmount() {
        return totalClaimAmount;
    }

    /**
     * Computes and returns the customer's membership tier based on total approved claims.
     *
     * @return the MembershipTier (SILVER, GOLD, or PLATINUM)
     */
    public MembershipTier getMembershipTier() {
        return MembershipTier.computeTier(totalClaimAmount);
    }

    /**
     * Calculates the estimated patient co-pay for a given claim amount,
     * applying the customer's membership tier discount to the standard 20% co-pay.
     *
     * @param claimAmount the total claim amount
     * @return the discounted patient co-pay amount
     */
    public double calculatePatientCopay(double claimAmount) {
        double standardCopay = claimAmount * 0.20;
        double discountRate = getMembershipTier().getDiscountRate();
        return standardCopay * (1.0 - discountRate);
    }

    /**
     * Sets the customer ID.
     *
     * @param customerId the customer ID to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Sets the customer identifier, providing compatibility with setId().
     *
     * @param id the customer ID to set
     */
    public void setId(String id) {
        this.customerId = id;
    }

    /**
     * Links an insurance card to this customer.
     *
     * @param insuranceCard the InsuranceCard to link
     */
    public void setInsuranceCard(InsuranceCard insuranceCard) {
        this.insuranceCard = insuranceCard;
    }

    /**
     * Sets the total claim amount for this customer.
     *
     * @param totalClaimAmount the total claim amount to set
     */
    public void setTotalClaimAmount(double totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
    }

    /**
     * Returns the customer type ("PolicyHolder" or "Dependent").
     *
     * @return the customer type string
     */
    public abstract String getCustomerType();

    /**
     * Gets the parent policy holder ID if this customer is a dependent.
     *
     * @return the parent PolicyHolder customerId, or null if not applicable
     */
    public abstract String getParentPolicyHolderId();

    /**
     * Formats the customer record into a 4-column CSV string for customers.txt persistence.
     * Format: customerId,fullName,type,parentId
     *
     * @return the CSV representation of the customer for customers.txt
     */
    public abstract String toCustomerFileString();

    /**
     * Formats the customer user account into an 8-column CSV string for users.txt persistence.
     * Format: userId,username,password,fullName,email,role,status,customerId
     *
     * @return the CSV representation of the customer user for users.txt
     */
    @Override
    public String toUserFileString() {
        return toUserBaseFileString() + "," + (customerId != null ? customerId : "");
    }

    /**
     * Formats the customer record for customers.txt persistence.
     * Delegates to toCustomerFileString() for backward compatibility.
     *
     * @return the CSV representation of the customer for customers.txt
     */
    @Override
    public String toFileString() {
        return toCustomerFileString();
    }

    @Override
    public String toString() {
        return "Customer{customerId=" + customerId
                + ", userId=" + getUserId()
                + ", name=" + getFullName()
                + ", type=" + getCustomerType()
                + ", totalClaimAmount=" + totalClaimAmount
                + "}";
    }
}
