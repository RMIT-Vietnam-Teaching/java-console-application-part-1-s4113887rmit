package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Read-only customer portal for profiles, cards, claims, and family relationships.
 */
final class CustomerPortal {
    private CustomerPortal() {
    }

    static void show(AppContext context, Customer customer, Scanner sc) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n========== CUSTOMER PORTAL ==========");
            System.out.println("1. View My Profile & Account Information");
            System.out.println("2. View My Insurance Card Details");
            System.out.println("3. View My Submitted Claims & Status");
            if (customer instanceof PolicyHolder) {
                System.out.println("4. View Family Dependents & Plan Summary");
            } else {
                System.out.println("4. View Covering PolicyHolder Details");
            }
            System.out.println("5. Logout to Login Screen");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    viewCustomerProfile(customer);
                    break;
                case "2":
                    viewCustomerCard(customer);
                    break;
                case "3":
                    viewCustomerClaims(context, customer);
                    break;
                case "4":
                    if (customer instanceof PolicyHolder) {
                        viewPolicyHolderDependents(context, (PolicyHolder) customer);
                    } else {
                        viewDependentParent(context, (Dependent) customer);
                    }
                    break;
                case "5":
                    System.out.println("Logging out from Customer Portal...");
                    inSession = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 5.");
            }
        }
    }

    private static void viewCustomerProfile(Customer customer) {
        System.out.println("\n----- My Profile & Account Information -----");
        System.out.println("Customer ID      : " + customer.getId());
        System.out.println("Full Name        : " + customer.getFullName());
        System.out.println("User ID          : " + (customer.getUserId() != null ? customer.getUserId() : "N/A"));
        System.out.println("Username         : " + (customer.getUsername() != null ? customer.getUsername() : "N/A"));
        System.out.println("Email            : " + (customer.getEmail() != null ? customer.getEmail() : "N/A"));
        System.out.println("Account Role     : " + customer.getRole());
        System.out.println("Account Status   : " + customer.getStatus());
        System.out.println("Customer Type    : " + customer.getCustomerType());
        System.out.println("Membership Tier  : " + customer.getMembershipTier());
        System.out.println("Co-pay Discount  : " + String.format("%.0f%%", customer.getMembershipTier().getDiscountRate() * 100));
        System.out.println("Approved Claims  : " + customer.getTotalClaimAmount());
    }

    private static void viewCustomerCard(Customer customer) {
        System.out.println("\n----- My Insurance Card Details -----");
        InsuranceCard card = customer.getInsuranceCard();
        if (card == null) {
            System.out.println("No insurance card linked to this account.");
        } else {
            System.out.println("Card Number      : " + card.getCardNumber());
            System.out.println("Card Holder ID   : " + card.getCardHolderId());
            System.out.println("Policy Owner ID  : " + card.getPolicyOwnerId());
            System.out.println("Expiration Date  : " + card.getExpirationDate());
        }
    }

    private static void viewCustomerClaims(AppContext context, Customer customer) {
        System.out.println("\n----- My Claims & History -----");
        System.out.println("Current Membership Tier: " + customer.getMembershipTier()
                + " (Co-pay Discount: " + String.format("%.0f%%", customer.getMembershipTier().getDiscountRate() * 100) + ")");
        List<Claim> all = context.getClaimRepository().getAll();
        int count = 0;
        for (Claim c : all) {
            if (c.getInsuredPersonId().equals(customer.getId())) {
                double stdCopay = c.getClaimAmount() * 0.20;
                double discountedCopay = customer.calculatePatientCopay(c.getClaimAmount());
                System.out.println("Claim ID: " + c.getId()
                        + " | Date: " + c.getClaimDate()
                        + " | Claim Amount: " + String.format("%,.2f VND", c.getClaimAmount())
                        + " | Status: [" + c.getStatus() + "]"
                        + " | Est. Patient Co-pay: " + String.format("%,.2f VND", discountedCopay)
                        + " (Saved: " + String.format("%,.2f VND", (stdCopay - discountedCopay)) + ")"
                        + " | Exam Date: " + c.getExamDate()
                        + " | Documents: " + c.getDocuments());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No claims found for your customer ID.");
        }
    }

    private static void viewPolicyHolderDependents(AppContext context, PolicyHolder ph) {
        System.out.println("\n----- Family Dependents Covered Under Your Plan -----");
        List<Dependent> deps = ph.getDependents();
        if (deps.isEmpty()) {
            System.out.println("No dependents registered under your plan.");
            return;
        }
        for (Dependent d : deps) {
            System.out.println("Dependent ID: " + d.getId()
                    + " | Name: " + d.getFullName()
                    + " | Email: " + d.getEmail()
                    + " | Card: " + (d.getInsuranceCard() != null ? d.getInsuranceCard().getCardNumber() : "None")
                    + " | Approved Claims: " + d.getTotalClaimAmount());
        }
    }

    private static void viewDependentParent(AppContext context, Dependent dep) {
        System.out.println("\n----- Covering PolicyHolder Details -----");
        String parentId = dep.getParentPolicyHolderId();
        if (parentId == null) {
            System.out.println("No covering PolicyHolder ID associated.");
            return;
        }
        Customer parent = context.getCustomerRepository().getById(parentId);
        if (parent != null) {
            System.out.println("PolicyHolder ID : " + parent.getId());
            System.out.println("Full Name       : " + parent.getFullName());
            System.out.println("Email           : " + parent.getEmail());
            System.out.println("Membership Tier : " + parent.getMembershipTier());
        } else {
            System.out.println("Covering PolicyHolder ID: " + parentId + " (Record not found)");
        }
    }

    // =========================================================================
    // ADMIN USER MANAGEMENT & SOFT-DELETE OPERATIONS
    // =========================================================================


}
