package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Main application console entry point for the ClaimShield Insurance System.
 * Supports authentication, role-based menus (Admin, Claims Officer, Customer),
 * full CRUD and queries, and persistence.
 */
public class Main {
    private static final String USERS_FILE = "data/users.txt";
    private static final String CUSTOMERS_FILE = "data/customers.txt";
    private static final String CARDS_FILE = "data/cards.txt";
    private static final String CLAIMS_FILE = "data/claims.txt";

    public static void main(String[] args) {
        // 1. Print exact required banner
        System.out.println("=========================================");
        System.out.println("COSC3110/3111 HEALTH INSURANCE SYSTEM");
        System.out.println("       Student ID: S4113887");
        System.out.println("     Student Name: Nguyen Ngoc Quang Dang");
        System.out.println("=========================================");

        // 2. Instantiate AppContext and load all datasets
        AppContext context = new AppContext();
        context.loadAll(USERS_FILE, CUSTOMERS_FILE, CARDS_FILE, CLAIMS_FILE);
        System.out.println("\nSystem loaded: "
                + context.getUserRepository().getAll().size() + " users, "
                + context.getCustomerRepository().getAll().size() + " customers, "
                + context.getCardRepository().getAll().size() + " cards, "
                + context.getClaimRepository().getAll().size() + " claims.");

        Scanner sc = new Scanner(System.in);

        // 3. Main Login & Session Loop
        boolean appRunning = true;
        while (appRunning) {
            System.out.println("\n===== LOGIN SYSTEM =====");
            System.out.print("Username (or 'exit' to quit): ");
            String username = sc.nextLine().trim();

            if ("exit".equalsIgnoreCase(username)) {
                System.out.println("Saving all data before exit...");
                context.saveAll(USERS_FILE, CUSTOMERS_FILE, CARDS_FILE, CLAIMS_FILE);
                System.out.println("All data saved successfully. Goodbye!");
                appRunning = false;
                break;
            }

            System.out.print("Password: ");
            String password = sc.nextLine();

            User currentUser = context.getUserRepository().authenticate(username, password);
            if (currentUser == null) {
                System.out
                        .println("Login failed! Invalid username, password, or account is inactive. Please try again.");
                continue;
            }

            // 4. On successful login: display role dashboard banner once
            currentUser.displayDashboard();

            // Branch by currentUser.getRole()
            switch (currentUser.getRole()) {
                case ADMIN:
                    adminMenuLoop(context, (Admin) currentUser, sc);
                    break;
                case OFFICER:
                    officerMenuLoop(context, (ClaimsOfficer) currentUser, sc);
                    break;
                case CUSTOMER:
                    customerMenuLoop(context, (Customer) currentUser, sc);
                    break;
                default:
                    System.out.println("Unknown user role encountered.");
            }
        }

        sc.close();
    }
    // =========================================================================
    // ADMIN MENU & OPERATIONS
    // =========================================================================

    private static void adminMenuLoop(AppContext context, Admin admin, Scanner sc) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n========== ADMIN MAIN MENU ==========");
            System.out.println("1. Manage Customer Directory (CRUD)");
            System.out.println("2. Manage Insurance Cards (CRUD)");
            System.out.println("3. Manage Claims (CRUD, Processing, Advanced Filters)");
            System.out.println("4. Manage User Accounts & Soft-Delete");
            System.out.println("5. View System Audit Log (Placeholder)");
            System.out.println("6. View Financial Statistics & Summary");
            System.out.println("7. Save All Changes to Files");
            System.out.println("8. Logout to Login Screen");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    adminCustomerSubMenu(context, sc);
                    break;
                case "2":
                    adminCardSubMenu(context, sc);
                    break;
                case "3":
                    adminClaimSubMenu(context, sc);
                    break;
                case "4":
                    adminUserSubMenu(context, sc);
                    break;
                case "5":
                    viewAuditLogPlaceholder();
                    break;
                case "6":
                    viewFinancialStatisticsPlaceholder(context);
                    break;
                case "7":
                    context.saveAll(USERS_FILE, CUSTOMERS_FILE, CARDS_FILE, CLAIMS_FILE);
                    System.out.println("All system datasets saved successfully to files.");
                    break;
                case "8":
                    System.out.println("Logging out from Admin session...");
                    inSession = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 8.");
            }
        }
    }

    private static void adminCustomerSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Customer Management -----");
            System.out.println("1. Add New PolicyHolder (with User Account)");
            System.out.println("2. Add New Dependent (with User Account)");
            System.out.println("3. View All Customers & Details");
            System.out.println("4. Toggle Customer Status (Soft-Delete / Deactivate / Reactivate)");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

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
                    adminSoftDeleteCustomerFlow(context, sc);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void adminAddPolicyHolderFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Register New PolicyHolder ---");
        System.out.print("User ID (format u-XXXXXXX): ");
        String userId = sc.nextLine().trim();

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        System.out.print("Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Customer ID (format c-XXXXXXX): ");
        String customerId = sc.nextLine().trim();

        boolean success = context.registerPolicyHolder(userId, username, password, fullName, email, customerId);
        if (success) {
            System.out.println("PolicyHolder and user account registered successfully!");
        } else {
            System.out.println(
                    "Failed to register PolicyHolder. Please check ID formats, non-empty fields, and duplicate IDs/usernames.");
        }
    }

    private static void adminAddDependentFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Register New Dependent ---");
        System.out.print("User ID (format u-XXXXXXX): ");
        String userId = sc.nextLine().trim();

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        System.out.print("Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Customer ID (format c-XXXXXXX): ");
        String customerId = sc.nextLine().trim();

        System.out.print("Parent PolicyHolder ID (c-XXXXXXX): ");
        String parentId = sc.nextLine().trim();

        boolean success = context.registerDependent(userId, username, password, fullName, email, customerId, parentId);
        if (success) {
            System.out.println("Dependent and user account registered successfully and linked to parent!");
        } else {
            System.out.println(
                    "Failed to register Dependent. Please check ID formats, ensure parent exists as a PolicyHolder, and verify no duplicate accounts.");
        }
    }

    private static void adminViewAllCustomers(AppContext context) {
        System.out.println("\n----- All Customers in System -----");
        List<Customer> all = context.getCustomerRepository().getAll();
        if (all.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : all) {
            System.out.println("ID: " + c.getId()
                    + " | Name: " + c.getFullName()
                    + " | Type: " + c.getCustomerType()
                    + " | Tier: " + c.getMembershipTier()
                    + " | Total Claim Amount: " + c.getTotalClaimAmount()
                    + " | Card: " + (c.getInsuranceCard() != null ? c.getInsuranceCard().getCardNumber() : "None")
                    + " | User: " + (c.getUsername() != null ? c.getUsername() : "N/A"));
        }
    }

    private static void adminSoftDeleteCustomerFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Toggle Customer Status (Soft-Delete) ---");
        System.out.print("Enter Customer ID to activate/deactivate: ");
        String id = sc.nextLine().trim();

        Customer c = context.getCustomerRepository().getById(id);
        if (c != null) {
            UserStatus newStatus = (c.getStatus() == UserStatus.ACTIVE) ? UserStatus.INACTIVE : UserStatus.ACTIVE;
            c.setStatus(newStatus);
            System.out.println("Customer " + id + " (" + c.getFullName() + ") account status updated to: " + newStatus
                    + " (Soft-Delete applied; historical claims and card records preserved).");
        } else {
            System.out.println("Customer not found with ID: " + id);
        }
    }

    private static void adminCardSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Insurance Card Management -----");
            System.out.println("1. Register New Card");
            System.out.println("2. View All Cards");
            System.out.println("3. Remove Card");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    addCardFlow(context, sc);
                    break;
                case "2":
                    viewAllCards(context);
                    break;
                case "3":
                    removeCardFlow(context, sc);
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addCardFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Register New Insurance Card ---");
        System.out.print("Card Number (10 digits): ");
        String cardNumber = sc.nextLine().trim();

        System.out.print("Card Holder Customer ID: ");
        String cardHolderId = sc.nextLine().trim();

        System.out.print("Policy Owner Customer ID: ");
        String policyOwnerId = sc.nextLine().trim();

        System.out.print("Expiration Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime expirationDate;
        try {
            expirationDate = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid date format. Card not registered.");
            return;
        }

        Customer holder = context.getCustomerRepository().getById(cardHolderId);
        Customer owner = context.getCustomerRepository().getById(policyOwnerId);

        if (holder == null || owner == null) {
            System.out.println("Failed to register card: Card holder or Policy owner does not exist.");
            return;
        }
        if (!Validator.isPolicyHolder(owner)) {
            System.out.println("Failed to register card: Policy owner must be a PolicyHolder.");
            return;
        }

        InsuranceCard card = new InsuranceCard(cardNumber, cardHolderId, policyOwnerId, expirationDate);
        boolean success = context.getCardRepository().add(card);
        if (success) {
            holder.setInsuranceCard(card);
            System.out.println("Insurance card registered and linked successfully.");
        } else {
            System.out.println("Failed to register card. Check 10-digit format and ensure card number is unique.");
        }
    }

    private static void viewAllCards(AppContext context) {
        System.out.println("\n----- All Insurance Cards -----");
        List<InsuranceCard> cards = context.getCardRepository().getAll();
        if (cards.isEmpty()) {
            System.out.println("No insurance cards found.");
            return;
        }
        for (InsuranceCard c : cards) {
            System.out.println(c);
        }
    }

    private static void removeCardFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Remove Insurance Card ---");
        System.out.print("Enter Card Number to remove: ");
        String cardNumber = sc.nextLine().trim();

        boolean success = context.getCardRepository().delete(cardNumber);
        if (success) {
            System.out.println("Insurance card removed successfully.");
        } else {
            System.out.println("Card not found with number: " + cardNumber);
        }
    }

    private static void adminClaimSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Claim Management -----");
            System.out.println("1. Create New Claim");
            System.out.println("2. Add Supporting Document to Claim");
            System.out.println("3. Update Claim Status (NEW -> PROCESSING -> DONE)");
            System.out.println("4. View All Claims");
            System.out.println("5. Filter Claims by Status");
            System.out.println("6. Filter Claims by Date Range");
            System.out.println("7. Filter Claims by PolicyHolder Family");
            System.out.println("8. Remove Claim");
            System.out.println("9. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    addClaimFlow(context, sc);
                    break;
                case "2":
                    addDocumentFlow(context, sc);
                    break;
                case "3":
                    updateClaimStatusFlow(context, sc);
                    break;
                case "4":
                    viewAllClaims(context);
                    break;
                case "5":
                    filterClaimsByStatusFlow(context, sc);
                    break;
                case "6":
                    filterClaimsByDateRangeFlow(context, sc);
                    break;
                case "7":
                    filterClaimsByFamilyFlow(context, sc);
                    break;
                case "8":
                    removeClaimFlow(context, sc);
                    break;
                case "9":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewSystemSummary(AppContext context) {
        System.out.println("\n===== SYSTEM SUMMARY =====");
        System.out.println("Total User Accounts     : " + context.getUserRepository().getAll().size());
        System.out.println("Total Customers         : " + context.getCustomerRepository().getAll().size());
        System.out.println("Total Insurance Cards   : " + context.getCardRepository().getAll().size());
        System.out.println("Total Claims            : " + context.getClaimRepository().getAll().size());

        System.out.println("\n--- All User Accounts in System ---");
        for (User u : context.getUserRepository().getAll()) {
            System.out.println("User ID: " + u.getUserId()
                    + " | Username: " + u.getUsername()
                    + " | Name: " + u.getFullName()
                    + " | Role: " + u.getRole()
                    + " | Status: " + u.getStatus());
        }
    }
    // =========================================================================
    // CLAIMS OFFICER MENU & OPERATIONS
    // =========================================================================

    private static void officerMenuLoop(AppContext context, ClaimsOfficer officer, Scanner sc) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n========== CLAIMS OFFICER MENU ==========");
            System.out.println("1. Process & Update Claim Status (NEW -> PROCESSING -> DONE)");
            System.out.println("2. Add Supporting Document to Claim");
            System.out.println("3. View All Claims");
            System.out.println("4. Filter Claims by Status");
            System.out.println("5. Filter Claims by Date Range");
            System.out.println("6. Filter Claims by PolicyHolder Family");
            System.out.println("7. View Customer Directory (Read-only)");
            System.out.println("8. View Insurance Cards (Read-only)");
            System.out.println("9. Save All Changes to Files");
            System.out.println("10. Logout to Login Screen");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    updateClaimStatusFlow(context, sc);
                    break;
                case "2":
                    addDocumentFlow(context, sc);
                    break;
                case "3":
                    viewAllClaims(context);
                    break;
                case "4":
                    filterClaimsByStatusFlow(context, sc);
                    break;
                case "5":
                    filterClaimsByDateRangeFlow(context, sc);
                    break;
                case "6":
                    filterClaimsByFamilyFlow(context, sc);
                    break;
                case "7":
                    adminViewAllCustomers(context);
                    break;
                case "8":
                    viewAllCards(context);
                    break;
                case "9":
                    context.saveAll(USERS_FILE, CUSTOMERS_FILE, CARDS_FILE, CLAIMS_FILE);
                    System.out.println("All system datasets saved successfully to files.");
                    break;
                case "10":
                    System.out.println("Logging out from Claims Officer session...");
                    inSession = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 10.");
            }
        }
    }
    // =========================================================================
    // CUSTOMER MENU & OPERATIONS (STRICTLY READ-ONLY)
    // =========================================================================

    private static void customerMenuLoop(AppContext context, Customer customer, Scanner sc) {
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
            String choice = sc.nextLine().trim();

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
        List<Claim> all = context.getClaimRepository().getAll();
        int count = 0;
        for (Claim c : all) {
            if (c.getInsuredPersonId().equals(customer.getId())) {
                System.out.println("Claim ID: " + c.getId()
                        + " | Date: " + c.getClaimDate()
                        + " | Amount: " + c.getClaimAmount()
                        + " | Status: " + c.getStatus()
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
    // ADMIN USER MANAGEMENT, SOFT-DELETE, AND PLACEHOLDER OPERATIONS
    // =========================================================================

    private static void adminUserSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: User Account Management -----");
            System.out.println("1. View All User Accounts");
            System.out.println("2. Toggle Account Status (Soft-Delete / Deactivate / Activate)");
            System.out.println("3. Search User by Username");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewSystemSummary(context);
                    break;
                case "2":
                    adminToggleUserStatusFlow(context, sc);
                    break;
                case "3":
                    adminSearchUserFlow(context, sc);
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void adminToggleUserStatusFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Toggle User Account Status (Soft-Delete) ---");
        System.out.print("Enter User ID to activate/deactivate: ");
        String userId = sc.nextLine().trim();

        User user = context.getUserRepository().getById(userId);
        if (user == null) {
            System.out.println("User not found with ID: " + userId);
            return;
        }

        UserStatus newStatus = (user.getStatus() == UserStatus.ACTIVE) ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        user.setStatus(newStatus);
        System.out.println(
                "User account " + user.getUserId() + " (" + user.getUsername() + ") status updated to: " + newStatus);
    }

    private static void adminSearchUserFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Search User Account ---");
        System.out.print("Enter Username: ");
        String uname = sc.nextLine().trim();

        User u = context.getUserRepository().getByUsername(uname);
        if (u != null) {
            System.out.println("User ID   : " + u.getUserId());
            System.out.println("Username  : " + u.getUsername());
            System.out.println("Full Name : " + u.getFullName());
            System.out.println("Email     : " + u.getEmail());
            System.out.println("Role      : " + u.getRole());
            System.out.println("Status    : " + u.getStatus());
        } else {
            System.out.println("No user found with username: " + uname);
        }
    }

    private static void viewAuditLogPlaceholder() {
        System.out.println("\n===== SYSTEM AUDIT LOG (VIEW ONLY) =====");
        System.out.println("[AUDIT LOG PLACEHOLDER]");
        System.out.println(
                "System audit logging service initialized. All role authentication attempts and status transitions are recorded.");
    }

    private static void viewFinancialStatisticsPlaceholder(AppContext context) {
        System.out.println("\n===== FINANCIAL STATISTICS & SUMMARY =====");
        double totalApproved = 0.0;
        double totalProcessing = 0.0;
        double totalNew = 0.0;

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                totalApproved += c.getClaimAmount();
            } else if (c.getStatus() == ClaimStatus.PROCESSING) {
                totalProcessing += c.getClaimAmount();
            } else if (c.getStatus() == ClaimStatus.NEW) {
                totalNew += c.getClaimAmount();
            }
        }

        System.out.println("Total Approved Claims (DONE)       : " + String.format("%,.2f VND", totalApproved));
        System.out.println("Total Processing Claims            : " + String.format("%,.2f VND", totalProcessing));
        System.out.println("Total Pending Claims (NEW)         : " + String.format("%,.2f VND", totalNew));
        System.out.println("Cumulative Claim Volume            : "
                + String.format("%,.2f VND", (totalApproved + totalProcessing + totalNew)));
    }
    // =========================================================================
    // CLAIM OPERATION FLOWS (WITH COMPLETE EXCEPTION HANDLING)
    // =========================================================================

    private static void addClaimFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Create New Claim ---");
        System.out.print("Claim ID (format f-XXXXXXXXXX): ");
        String id = sc.nextLine().trim();

        System.out.print("Claim Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime claimDate;
        try {
            claimDate = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid claim date format. Claim not created.");
            return;
        }

        System.out.print("Insured Person Customer ID: ");
        String insuredPersonId = sc.nextLine().trim();

        System.out.print("Card Number (10 digits): ");
        String cardNumber = sc.nextLine().trim();

        System.out.print("Exam Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime examDate;
        try {
            examDate = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid exam date format. Claim not created.");
            return;
        }

        System.out.print("Claim Amount: ");
        double claimAmount;
        try {
            claimAmount = Double.parseDouble(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid amount format. Claim not created.");
            return;
        }

        Claim claim = new Claim(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount, ClaimStatus.NEW);
        try {
            boolean success = context.getClaimRepository().add(claim);
            if (success) {
                System.out.println("Claim created successfully with status NEW.");
            } else {
                System.out.println(
                        "Failed to create claim. Please check ID format, duplicate ID, references, and positive amount.");
            }
        } catch (InvalidClaimDateException e) {
            System.out.println("Business Rule Violation (Invalid Claim Date): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating claim: " + e.getMessage());
        }
    }

    private static void addDocumentFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Add Supporting Document to Claim ---");
        System.out.print("Claim ID: ");
        String claimId = sc.nextLine().trim();

        System.out.print("Document Name (format ClaimId_CardNumber_Name.pdf): ");
        String documentName = sc.nextLine().trim();

        boolean success = context.getClaimRepository().addDocument(claimId, documentName);
        if (success) {
            System.out.println("Document added successfully to claim " + claimId + ".");
        } else {
            System.out.println(
                    "Failed to add document. Please verify claim ID exists and document format matches ClaimId_CardNumber_Name.pdf.");
        }
    }

    private static void updateClaimStatusFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Update Claim Status ---");
        System.out.print("Claim ID: ");
        String claimId = sc.nextLine().trim();

        System.out.print("New Status (NEW / PROCESSING / DONE): ");
        String newStatusStr = sc.nextLine().trim();

        ClaimStatus newStatus = ClaimStatus.fromString(newStatusStr);
        if (newStatus == null) {
            System.out.println("Unrecognized status: '" + newStatusStr + "'. Allowed values: NEW, PROCESSING, DONE.");
            return;
        }

        try {
            boolean success = context.getClaimRepository().updateClaimStatus(claimId, newStatus);
            if (success) {
                System.out.println("Claim status updated successfully to " + newStatus + ".");
            } else {
                System.out.println("Failed to update status. Claim not found with ID: " + claimId);
            }
        } catch (InvalidStatusTransitionException e) {
            System.out.println("Business Rule Violation (Invalid Status Transition): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating claim status: " + e.getMessage());
        }
    }

    private static void viewAllClaims(AppContext context) {
        System.out.println("\n----- All Claims in System -----");
        List<Claim> claims = context.getClaimRepository().getAll();
        if (claims.isEmpty()) {
            System.out.println("No claims found.");
            return;
        }
        for (Claim c : claims) {
            System.out.println(c);
        }
    }

    private static void filterClaimsByStatusFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by Status ---");
        System.out.print("Status to filter (NEW / PROCESSING / DONE): ");
        String statusStr = sc.nextLine().trim();
        ClaimStatus status = ClaimStatus.fromString(statusStr);
        if (status == null) {
            System.out.println("Invalid status: " + statusStr);
            return;
        }

        List<Claim> filtered = context.getClaimRepository().filterByStatus(status);
        System.out.println("Found " + filtered.size() + " claim(s) with status " + status + ":");
        for (Claim c : filtered) {
            System.out.println(c);
        }
    }

    private static void filterClaimsByDateRangeFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by Date Range ---");
        System.out.print("Start Date-Time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid start date format.");
            return;
        }

        System.out.print("End Date-Time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end;
        try {
            end = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid end date format.");
            return;
        }

        List<Claim> filtered = context.getClaimRepository().filterByDateRange(start, end);
        System.out.println("Found " + filtered.size() + " claim(s) between " + start + " and " + end + ":");
        for (Claim c : filtered) {
            System.out.println(c);
        }
    }

    private static void filterClaimsByFamilyFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by PolicyHolder Family ---");
        System.out.print("Enter PolicyHolder Customer ID (c-XXXXXXX): ");
        String policyHolderId = sc.nextLine().trim();

        List<Claim> filtered = context.getClaimRepository().filterByPolicyHolderFamily(policyHolderId);
        System.out.println("Found " + filtered.size() + " claim(s) for family of PolicyHolder " + policyHolderId + ":");
        for (Claim c : filtered) {
            System.out.println(c);
        }
    }

    private static void removeClaimFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Remove Claim ---");
        System.out.print("Enter Claim ID to remove: ");
        String claimId = sc.nextLine().trim();

        boolean success = context.getClaimRepository().delete(claimId);
        if (success) {
            System.out.println("Claim removed successfully.");
        } else {
            System.out.println("Claim not found with ID: " + claimId);
        }
    }
}
