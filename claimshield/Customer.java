package claimshield;
/**
 * Represents a customer in the ClaimShield system, either a
 * PolicyHolder or a Dependent linked to a PolicyHolder.
 *
 * @author Nguyen Ngoc Quang Dang - S4113887
 */
public class Customer {
    private String id;
    private String fullName;
    private String customerType;
    private String parentPolicyHolderId;

    public Customer(String id,String fullName,String customerType,String parentPolicyHolderId) {
        this.id = id;
        this.fullName = fullName;
        this.customerType = customerType;
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    public String getId() {
        return id;
    }
    public String getFullName() {
        return fullName;
    }
    public String getCustomerType() {
        return customerType;
    }
    public String getParentPolicyHolderId() {
        return parentPolicyHolderId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    public void setParentPolicyHolderId(String parentPolicyHolderId) {
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    public String toFileString() {
        return id + "," + fullName + "," + customerType + "," + parentPolicyHolderId;
    }
    @Override
    public String toString() {
        return "Customer{id=" + id + ", name=" + fullName + ", type=" + customerType + "}";
    }

}
