package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Console operations focused on customer registration, search, display, and maintenance.
 */
final class CustomerConsole {
    private CustomerConsole() {
    }

    static void adminCustomerSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Customer Management -----");
            System.out.println("1. Add New PolicyHolder (with User Account)");
            System.out.println("2. Add New Dependent (with User Account)");
            System.out.println("3. View All Customers & Details");
            System.out.println("4. View Customer by ID");
            System.out.println("5. Search Customer by Name");
            System.out.println("6. List Customers by Type (PolicyHolder / Dependent)");
            System.out.println("7. List Dependents Covered by a PolicyHolder");
            System.out.println("8. Update Customer Details");
            System.out.println("9. Toggle Customer Status (Soft-Delete / Deactivate / Reactivate)");
            System.out.println("10. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    adminAddPolicyHolderFlow(context, sc);
                    break;
                case "2":
                    adminAddDependentFlow(context, sc);
                    break;
                case "3":
                    adminViewAllCustomers(context);
                    break;
                case "4":
                    viewCustomerByIdFlow(context, sc);
                    break;
                case "5":
                    searchCustomerByNameFlow(context, sc);
                    break;
                case "6":
                    listCustomersByTypeFlow(context, sc);
                    break;
                case "7":
                    listDependentsOfPolicyHolderFlow(context, sc);
                    break;
                case "8":
                    adminUpdateCustomerFlow(context, sc);
                    break;
                case "9":
                    adminSoftDeleteCustomerFlow(context, sc);
                    break;
                case "10":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Admin flow for registering a new PolicyHolder together with the user
     * account that grants them access to the customer portal.
     * <p>
     * Every free-text field is length-checked before the request reaches
     * {@link AppContext#registerPolicyHolder}, so the operator is told which
     * field was rejected instead of receiving a generic failure message.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void adminAddPolicyHolderFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Register New PolicyHolder ---");
        System.out.print("User ID (format u-XXXXXXX): ");
        String userId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Username: ");
        String username = ConsoleSupport.readLine(sc).trim();

        System.out.print("Password: ");
        String password = ConsoleSupport.readLine(sc).trim();

        System.out.print("Full Name: ");
        String fullName = ConsoleSupport.readLine(sc).trim();

        System.out.print("Email: ");
        String email = ConsoleSupport.readLine(sc).trim();

        System.out.print("Customer ID (format c-XXXXXXX): ");
        String customerId = ConsoleSupport.readLine(sc).trim();

        // Enforce the shared free-text length rules. This rejects both blank
        // and over-long input in one step, naming the offending field.
        String fieldError = ConsoleSupport.validateAccountFields(username, password, fullName, email);
        if (fieldError != null) {
            System.out.println("Error: " + fieldError + " Registration aborted.");
            return;
        }

        boolean success = context.registerPolicyHolder(userId, username, password, fullName, email, customerId);
        if (success) {
            context.autoSave();
            System.out.println("PolicyHolder and user account registered successfully! (Data auto-saved to files)");
        } else {
            System.out.println("Failed to register PolicyHolder. Please check ID formats, non-empty fields, and duplicate IDs/usernames.");
        }
    }


    /**
     * Admin flow for registering a new Dependent and linking it to an existing
     * PolicyHolder.
     * <p>
     * Applies the same free-text length rules as the PolicyHolder flow so both
     * registration paths behave consistently for the operator.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void adminAddDependentFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Register New Dependent ---");
        System.out.print("User ID (format u-XXXXXXX): ");
        String userId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Username: ");
        String username = ConsoleSupport.readLine(sc).trim();

        System.out.print("Password: ");
        String password = ConsoleSupport.readLine(sc).trim();

        System.out.print("Full Name: ");
        String fullName = ConsoleSupport.readLine(sc).trim();

        System.out.print("Email: ");
        String email = ConsoleSupport.readLine(sc).trim();

        System.out.print("Customer ID (format c-XXXXXXX): ");
        String customerId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Parent PolicyHolder ID (c-XXXXXXX): ");
        String parentId = ConsoleSupport.readLine(sc).trim();

        // Enforce the shared free-text length rules before registering.
        String fieldError = ConsoleSupport.validateAccountFields(username, password, fullName, email);
        if (fieldError != null) {
            System.out.println("Error: " + fieldError + " Registration aborted.");
            return;
        }

        boolean success = context.registerDependent(userId, username, password, fullName, email, customerId, parentId);
        if (success) {
            context.autoSave();
            System.out.println("Dependent and user account registered successfully and linked to parent! (Data auto-saved to files)");
        } else {
            System.out.println("Failed to register Dependent. Please check ID formats, ensure parent exists as a PolicyHolder, and verify no duplicate accounts.");
        }
    }

    static void adminViewAllCustomers(AppContext context) {
        System.out.println("\n----- All Customers in System -----");
        List<Customer> all = context.getCustomerRepository().getAll();
        if (all.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : all) {
            printCustomerRow(c);
        }
    }

    /**
     * Prints a single customer in the shared one-line directory format reused by
     * every customer listing and search result screen.
     *
     * @param c the customer to render
     */
    private static void printCustomerRow(Customer c) {
        System.out.println("ID: " + c.getId()
                + " | Name: " + c.getFullName()
                + " | Type: " + c.getCustomerType()
                + " | Tier: " + c.getMembershipTier()
                + " | Total Claim Amount: " + c.getTotalClaimAmount()
                + " | Card: " + (c.getInsuranceCard() != null ? c.getInsuranceCard().getCardNumber() : "None")
                + " | User: " + (c.getUsername() != null ? c.getUsername() : "N/A"));
    }

    /**
     * Admin flow for the CustomerManageable name search filter.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void searchCustomerByNameFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Search Customer by Name ---");
        System.out.print("Enter name (or part of a name): ");
        String keyword = ConsoleSupport.readLine(sc).trim();

        List<Customer> matches = context.getCustomerRepository().searchByName(keyword);
        if (matches.isEmpty()) {
            System.out.println("No customers found matching '" + keyword + "'.");
            return;
        }
        System.out.println("Found " + matches.size() + " customer(s) matching '" + keyword + "':");
        for (Customer c : matches) {
            printCustomerRow(c);
        }
    }

    /**
     * Admin flow for the CustomerManageable type filter, which reports over the
     * PolicyHolder / Dependent half of the Customer hierarchy.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void listCustomersByTypeFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- List Customers by Type ---");
        System.out.print("Customer type (PolicyHolder / Dependent): ");
        String type = ConsoleSupport.readLine(sc).trim();

        List<Customer> matches = context.getCustomerRepository().filterByType(type);
        if (matches.isEmpty()) {
            System.out.println("No customers of type '" + type + "'. Valid types: PolicyHolder, Dependent.");
            return;
        }
        System.out.println("Found " + matches.size() + " customer(s) of type '" + type + "':");
        for (Customer c : matches) {
            printCustomerRow(c);
        }
    }

    /**
     * Admin flow for the CustomerManageable dependents filter, resolving a family
     * group from the covering PolicyHolder's customer ID.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void listDependentsOfPolicyHolderFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Dependents Covered by a PolicyHolder ---");
        System.out.print("Enter PolicyHolder Customer ID (c-XXXXXXX): ");
        String policyHolderId = ConsoleSupport.readLine(sc).trim();

        Customer parent = context.getCustomerRepository().getById(policyHolderId);
        if (parent == null) {
            System.out.println("No customer found with ID: " + policyHolderId);
            return;
        }
        if (!Validator.isPolicyHolder(parent)) {
            System.out.println("Customer " + policyHolderId + " (" + parent.getFullName()
                    + ") is a Dependent, not a PolicyHolder, and cannot cover a family group.");
            return;
        }

        List<Customer> dependents = context.getCustomerRepository().filterByParentPolicyHolder(policyHolderId);
        System.out.println("PolicyHolder: " + parent.getFullName() + " (" + policyHolderId + ")");
        if (dependents.isEmpty()) {
            System.out.println("No dependents registered under this policy.");
            return;
        }
        System.out.println("Found " + dependents.size() + " dependent(s):");
        for (Customer c : dependents) {
            printCustomerRow(c);
        }
    }

    /**
     * Admin flow for the CardManageable card-holder lookup.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */

    private static void adminUpdateCustomerFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Update Customer Details ---");
        System.out.print("Customer ID to update: ");
        String customerId = ConsoleSupport.readLine(sc).trim();

        Customer customer = context.getCustomerRepository().getById(customerId);
        if (customer == null) {
            System.out.println("Customer not found with ID: " + customerId);
            return;
        }

        System.out.println("Editing customer: " + customer.getFullName()
                + " (Type: " + customer.getCustomerType() + ")");
        System.out.println("Leave a field blank and press Enter to keep its current value.");

        System.out.print("New Full Name [" + customer.getFullName() + "]: ");
        String fullName = ConsoleSupport.readLine(sc).trim();
        String targetFullName = fullName.isEmpty() ? customer.getFullName() : fullName;

        System.out.print("New Email [" + customer.getEmail() + "]: ");
        String email = ConsoleSupport.readLine(sc).trim();
        String targetEmail = email.isEmpty() ? customer.getEmail() : email;

        System.out.print("New Username [" + customer.getUsername() + "]: ");
        String username = ConsoleSupport.readLine(sc).trim();
        String targetUsername = username.isEmpty() ? customer.getUsername() : username;

        System.out.print("New Password (blank to keep current): ");
        String password = ConsoleSupport.readLine(sc).trim();
        String targetPassword = password.isEmpty() ? customer.getPassword() : password;

        String fieldError = ConsoleSupport.validateAccountFields(targetUsername, targetPassword, targetFullName, targetEmail);
        if (fieldError != null) {
            System.out.println("Error: " + fieldError + " Update aborted.");
            return;
        }

        if (!username.isEmpty()) {
            User clash = context.getUserRepository().getByUsername(username);
            if (clash != null && !clash.getUserId().equals(customer.getUserId())) {
                System.out.println("Username '" + username + "' is already taken. Keeping the current username.");
                targetUsername = customer.getUsername();
            }
        }

        customer.setFullName(targetFullName);
        customer.setEmail(targetEmail);
        customer.setUsername(targetUsername);
        if (!password.isEmpty()) {
            customer.setPassword(targetPassword);
        }

        if (customer instanceof Dependent) {
            System.out.print("New Parent PolicyHolder ID [" + customer.getParentPolicyHolderId() + "]: ");
            String parentId = ConsoleSupport.readLine(sc).trim();
            if (!parentId.isEmpty()) {
                if (!Validator.isValidCustomerId(parentId)) {
                    System.out.println("Invalid customer ID format. Keeping the current parent.");
                } else {
                    Customer newParent = context.getCustomerRepository().getById(parentId);
                    if (!(newParent instanceof PolicyHolder)) {
                        System.out.println("Target customer is not a PolicyHolder. Keeping the current parent.");
                    } else {
                        Dependent dependent = (Dependent) customer;
                        Customer oldParent = context.getCustomerRepository()
                                .getById(dependent.getParentPolicyHolderId());
                        if (oldParent instanceof PolicyHolder) {
                            ((PolicyHolder) oldParent).removeDependent(dependent);
                        }
                        dependent.setParentPolicyHolderId(parentId);
                        ((PolicyHolder) newParent).addDependent(dependent);
                    }
                }
            }
        }

        context.getCustomerRepository().update(customer);
        context.autoSave();
        System.out.println("Customer " + customer.getCustomerId() + " updated successfully. (Data auto-saved to files)");
    }

    /**
     * Admin flow for editing an insurance card's expiration date and policy owner.
     * Blank input keeps the current value for that field.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */

    private static void viewCustomerByIdFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- View Customer by ID ---");
        System.out.print("Enter Customer ID: ");
        String id = ConsoleSupport.readLine(sc).trim();

        Customer c = context.getCustomerRepository().getById(id);
        if (c != null) {
            System.out.println(c);
        } else {
            System.out.println("Customer not found with ID: " + id);
        }
    }

    private static void adminSoftDeleteCustomerFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Toggle Customer Status (Soft-Delete) ---");
        System.out.print("Enter Customer ID to activate/deactivate: ");
        String id = ConsoleSupport.readLine(sc).trim();

        Customer c = context.getCustomerRepository().getById(id);
        if (c != null) {
            UserStatus newStatus = (c.getStatus() == UserStatus.ACTIVE) ? UserStatus.INACTIVE : UserStatus.ACTIVE;
            c.setStatus(newStatus);
            AuditLogger.log(AppContext.getCurrentActorId(), "SOFT_DELETE_CUSTOMER_" + newStatus, id);
            context.autoSave();
            System.out.println("Customer " + id + " (" + c.getFullName() + ") account status updated to: " + newStatus
                    + " (Soft-Delete applied; historical records preserved; data auto-saved to files).");
        } else {
            System.out.println("Customer not found with ID: " + id);
        }
    }




}
