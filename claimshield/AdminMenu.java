package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Role-specific menu coordinator for administrative sessions. Delegates
 * customer, card, and claim operations to focused console classes.
 */
final class AdminMenu {
    private AdminMenu() {
    }

    static void show(AppContext context, Admin admin, Scanner sc) {
        // Record the start of this administrative session in the audit trail
        AuditLogger.log(admin.getUserId(), "ADMIN_SESSION_START", admin.getUserId());
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n========== ADMIN MAIN MENU ==========");
            System.out.println("1. Manage Customer Directory (CRUD)");
            System.out.println("2. Manage Insurance Cards (CRUD)");
            System.out.println("3. Manage Claims (CRUD, Processing, Advanced Filters)");
            System.out.println("4. Manage User Accounts & Soft-Delete");
            System.out.println("5. View System Audit Log");
            System.out.println("6. Financial Analytics & Reports");
            System.out.println("7. Save All Changes to Files");
            System.out.println("8. Logout to Login Screen");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    CustomerConsole.adminCustomerSubMenu(context, sc);
                    break;
                case "2":
                    CardConsole.adminCardSubMenu(context, sc);
                    break;
                case "3":
                    adminClaimSubMenu(context, sc);
                    break;
                case "4":
                    adminUserSubMenu(context, sc);
                    break;
                case "5":
                    viewAuditLog();
                    break;
                case "6":
                    adminReportsSubMenu(context, sc);
                    break;
                case "7":
                    context.saveAll(ConsoleSupport.USERS_FILE, ConsoleSupport.CUSTOMERS_FILE, ConsoleSupport.CARDS_FILE, ConsoleSupport.CLAIMS_FILE);
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

    private static void adminClaimSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Claim Management -----");
            System.out.println("1. Create New Claim");
            System.out.println("2. Add Supporting Document to Claim");
            System.out.println("3. Update Claim Status (NEW -> PROCESSING -> DONE)");
            System.out.println("4. View All Claims");
            System.out.println("5. View Claim by ID");
            System.out.println("6. Filter Claims by Status");
            System.out.println("7. Filter Claims by Date Range");
            System.out.println("8. Filter Claims by PolicyHolder Family");
            System.out.println("9. Remove Claim");
            System.out.println("10. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    ClaimConsole.addClaimFlow(context, sc);
                    break;
                case "2":
                    ClaimConsole.addDocumentFlow(context, sc);
                    break;
                case "3":
                    ClaimConsole.updateClaimStatusFlow(context, sc);
                    break;
                case "4":
                    ClaimConsole.viewAllClaims(context);
                    break;
                case "5":
                    ClaimConsole.viewClaimByIdFlow(context, sc);
                    break;
                case "6":
                    ClaimConsole.filterClaimsByStatusFlow(context, sc);
                    break;
                case "7":
                    ClaimConsole.filterClaimsByDateRangeFlow(context, sc);
                    break;
                case "8":
                    ClaimConsole.filterClaimsByFamilyFlow(context, sc);
                    break;
                case "9":
                    ClaimConsole.removeClaimFlow(context, sc);
                    break;
                case "10":
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

    private static void adminUserSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: User Account Management -----");
            System.out.println("1. View All User Accounts");
            System.out.println("2. Add New Administrator Account");
            System.out.println("3. Add New Claims Officer Account");
            System.out.println("4. Update User Account Details");
            System.out.println("5. Toggle Account Status (Soft-Delete / Deactivate / Activate)");
            System.out.println("6. Search User by Username");
            System.out.println("7. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    viewSystemSummary(context);
                    break;
                case "2":
                    adminAddStaffFlow(context, sc, UserRole.ADMIN);
                    break;
                case "3":
                    adminAddStaffFlow(context, sc, UserRole.OFFICER);
                    break;
                case "4":
                    adminUpdateUserFlow(context, sc);
                    break;
                case "5":
                    adminToggleUserStatusFlow(context, sc);
                    break;
                case "6":
                    adminSearchUserFlow(context, sc);
                    break;
                case "7":
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
        String userId = ConsoleSupport.readLine(sc).trim();

        User user = context.getUserRepository().getById(userId);
        if (user == null) {
            System.out.println("User not found with ID: " + userId);
            return;
        }

        UserStatus newStatus = (user.getStatus() == UserStatus.ACTIVE) ? UserStatus.INACTIVE : UserStatus.ACTIVE;

        if (newStatus == UserStatus.INACTIVE) {
            // An operator must never be able to lock themselves out of the system.
            User currentActor = AppContext.getCurrentSessionUser();
            if (currentActor != null && userId.equals(currentActor.getUserId())) {
                System.out.println("Cannot deactivate account " + userId
                        + ": this is the account you are currently signed in with.");
                return;
            }
            // The system must always keep at least one ACTIVE administrator able to log in.
            if (user.getRole() == UserRole.ADMIN && countActiveAdmins(context) <= 1) {
                System.out.println("Cannot deactivate administrator " + userId
                        + ": at least one ACTIVE administrator account must remain.");
                return;
            }
            // A Customer shares this very account object, so the family-cover rule that
            // applies in the customer directory has to apply here too; otherwise the
            // customer guard could be bypassed by editing the account instead.
            if (user instanceof PolicyHolder && ((PolicyHolder) user).hasActiveDependents()) {
                System.out.println("Cannot deactivate PolicyHolder " + ((Customer) user).getId()
                        + ": still covers active dependents. Deactivate those dependents first.");
                return;
            }
        }

        user.setStatus(newStatus);
        AuditLogger.log(AppContext.getCurrentActorId(), "TOGGLE_USER_STATUS_" + newStatus, userId);
        context.autoSave();
        System.out.println("User account " + user.getUserId() + " (" + user.getUsername() + ") status updated to: " + newStatus
                + " (Data auto-saved to files)");
    }

    /** Counts administrator accounts that are currently able to log in. */
    private static int countActiveAdmins(AppContext context) {
        int count = 0;
        for (User u : context.getUserRepository().getAll()) {
            if (u.getRole() == UserRole.ADMIN && u.getStatus() == UserStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    private static void adminSearchUserFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Search User Account ---");
        System.out.print("Enter Username: ");
        String uname = ConsoleSupport.readLine(sc).trim();

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

    /**
     * Admin flow for provisioning a new staff account (ADMIN or OFFICER).
     * Collects the account details, delegates validation and registration to
     * AppContext, then persists immediately.
     * <p>
     * Applies the same free-text length rules as every other registration flow.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     * @param role    the staff role to create (ADMIN or OFFICER)
     */
    private static void adminAddStaffFlow(AppContext context, Scanner sc, UserRole role) {
        String roleLabel = (role == UserRole.ADMIN) ? "Administrator" : "Claims Officer";
        System.out.println("\n--- Register New " + roleLabel + " Account ---");
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

        // Enforce the shared free-text length rules before creating the account.
        String fieldError = ConsoleSupport.validateAccountFields(username, password, fullName, email);
        if (fieldError != null) {
            System.out.println("Error: " + fieldError + " Registration aborted.");
            return;
        }

        boolean created = (role == UserRole.ADMIN)
                ? context.registerAdmin(userId, username, password, fullName, email)
                : context.registerOfficer(userId, username, password, fullName, email);

        if (created) {
            context.autoSave();
            System.out.println(roleLabel + " account '" + username + "' created successfully. (Data auto-saved to files)");
        } else {
            System.out.println("Failed to create " + roleLabel + " account. Verify that the User ID matches "
                    + "u-XXXXXXX, every field is non-blank, and neither the User ID nor the username is taken.");
        }
    }

    /**
     * Admin flow for editing an existing user account's profile and credentials.
     * Blank input keeps the current value for that field.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void adminUpdateUserFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Update User Account Details ---");
        System.out.print("User ID to update: ");
        String userId = ConsoleSupport.readLine(sc).trim();

        User user = context.getUserRepository().getById(userId);
        if (user == null) {
            System.out.println("User not found with ID: " + userId);
            return;
        }

        System.out.println("Editing account: " + user.getUsername() + " (Role: " + user.getRole() + ")");
        System.out.println("Leave a field blank and press Enter to keep its current value.");

        System.out.print("New Full Name [" + user.getFullName() + "]: ");
        String fullName = ConsoleSupport.readLine(sc).trim();
        String targetFullName = fullName.isEmpty() ? user.getFullName() : fullName;

        System.out.print("New Email [" + user.getEmail() + "]: ");
        String email = ConsoleSupport.readLine(sc).trim();
        String targetEmail = email.isEmpty() ? user.getEmail() : email;

        System.out.print("New Username [" + user.getUsername() + "]: ");
        String username = ConsoleSupport.readLine(sc).trim();
        String targetUsername = username.isEmpty() ? user.getUsername() : username;

        System.out.print("New Password (blank to keep current): ");
        String password = ConsoleSupport.readLine(sc).trim();
        String targetPassword = password.isEmpty() ? user.getPassword() : password;

        String fieldError = ConsoleSupport.validateAccountFields(targetUsername, targetPassword, targetFullName, targetEmail);
        if (fieldError != null) {
            System.out.println("Error: " + fieldError + " Update aborted.");
            return;
        }

        if (!username.isEmpty()) {
            User clash = context.getUserRepository().getByUsername(username);
            if (clash != null && !clash.getUserId().equals(user.getUserId())) {
                System.out.println("Username '" + username + "' is already taken. Keeping the current username.");
                targetUsername = user.getUsername();
            }
        }

        user.setFullName(targetFullName);
        user.setEmail(targetEmail);
        user.setUsername(targetUsername);
        if (!password.isEmpty()) {
            user.setPassword(targetPassword);
        }

        context.getUserRepository().update(user);
        context.autoSave();
        System.out.println("User account " + user.getUserId() + " updated successfully. (Data auto-saved to files)");
    }

    /**
     * Admin flow for editing an existing customer's profile and login credentials.
     * Because the Customer instance is shared between CustomerRepository and
     * UserRepository, a single repository update keeps both records consistent.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */

    private static void viewAuditLog() {
        System.out.println("\n===== SYSTEM AUDIT LOG (MOST RECENT FIRST) =====");
        List<AuditLogger.AuditEntry> logs = AuditLogger.readLogsMostRecentFirst();
        if (logs.isEmpty()) {
            System.out.println("No audit log entries recorded yet in data/logs.txt.");
            return;
        }
        System.out.println("Total Audit Log Entries: " + logs.size());
        System.out.println("------------------------------------------------------------------------------------------------");
        for (AuditLogger.AuditEntry entry : logs) {
            System.out.println(entry);
        }
        System.out.println("------------------------------------------------------------------------------------------------");
    }

    // =========================================================================
    // FINANCIAL ANALYTICS & REPORTS SUBMENU
    // =========================================================================

    private static void adminReportsSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n========== FINANCIAL ANALYTICS & REPORTS ==========");
            System.out.println("1. Overall Claims Volume & Breakdown by Status");
            System.out.println("2. Total Approved (DONE) Payout by Timeframe (Day/Week/Month/Custom)");
            System.out.println("3. Total Claim Payout Processed per Claims Officer");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    viewOverallClaimsVolumeReport(context);
                    break;
                case "2":
                    viewPayoutByTimeframeSubMenu(context, sc);
                    break;
                case "3":
                    viewPayoutPerOfficerReport(context);
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewOverallClaimsVolumeReport(AppContext context) {
        System.out.println("\n===== OVERALL CLAIMS VOLUME & BREAKDOWN =====");
        double totalApproved = 0.0;
        double totalProcessing = 0.0;
        double totalNew = 0.0;
        int countApproved = 0;
        int countProcessing = 0;
        int countNew = 0;

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                totalApproved += c.getClaimAmount();
                countApproved++;
            } else if (c.getStatus() == ClaimStatus.PROCESSING) {
                totalProcessing += c.getClaimAmount();
                countProcessing++;
            } else if (c.getStatus() == ClaimStatus.NEW) {
                totalNew += c.getClaimAmount();
                countNew++;
            }
        }

        System.out.println("Total Approved Claims (DONE)       : " + countApproved + " claim(s) | " + String.format("%,.2f VND", totalApproved));
        System.out.println("Total Processing Claims            : " + countProcessing + " claim(s) | " + String.format("%,.2f VND", totalProcessing));
        System.out.println("Total Pending Claims (NEW)         : " + countNew + " claim(s) | " + String.format("%,.2f VND", totalNew));
        System.out.println("Cumulative Claim Volume            : " + (countApproved + countProcessing + countNew) + " claim(s) | "
                + String.format("%,.2f VND", (totalApproved + totalProcessing + totalNew)));
    }

    private static void viewPayoutByTimeframeSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Approved (DONE) Payout by Timeframe ---");
            System.out.println("1. Today");
            System.out.println("2. This Week (Past 7 Days)");
            System.out.println("3. This Month (Current Calendar Month)");
            System.out.println("4. Custom Date Range");
            System.out.println("5. Back to Reports Menu");
            System.out.print("Choose a window: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            LocalDateTime start = null;
            LocalDateTime end = null;
            String windowLabel = "";

            switch (choice) {
                case "1":
                    start = java.time.LocalDate.now().atStartOfDay();
                    end = java.time.LocalDate.now().atTime(23, 59, 59);
                    windowLabel = "Today (" + java.time.LocalDate.now() + ")";
                    break;
                case "2":
                    start = java.time.LocalDate.now().minusDays(6).atStartOfDay();
                    end = java.time.LocalDate.now().atTime(23, 59, 59);
                    windowLabel = "This Week (" + start.toLocalDate() + " to " + end.toLocalDate() + ")";
                    break;
                case "3":
                    start = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    end = java.time.LocalDate.now().atTime(23, 59, 59);
                    windowLabel = "This Month (" + start.toLocalDate() + " to " + end.toLocalDate() + ")";
                    break;
                case "4":
                    System.out.print("Start Date-Time (yyyy-MM-ddTHH:mm): ");
                    try {
                        start = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
                    } catch (Exception e) {
                        System.out.println("Invalid start date format.");
                        continue;
                    }
                    System.out.print("End Date-Time (yyyy-MM-ddTHH:mm): ");
                    try {
                        end = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
                    } catch (Exception e) {
                        System.out.println("Invalid end date format.");
                        continue;
                    }
                    windowLabel = "Custom Range (" + start + " to " + end + ")";
                    break;
                case "5":
                    back = true;
                    continue;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 5.");
                    continue;
            }

            if (start != null && end != null) {
                double payout = context.getClaimRepository().getApprovedPayoutByDateRange(start, end);
                List<Claim> approvedClaims = context.getClaimRepository().getApprovedClaimsByDateRange(start, end);

                System.out.println("\n===== APPROVED PAYOUT REPORT: " + windowLabel + " =====");
                System.out.println("Total Approved Claims Found : " + approvedClaims.size());
                System.out.println("Total Approved Payout Amount: " + String.format("%,.2f VND", payout));
                if (!approvedClaims.isEmpty()) {
                    System.out.println("--- Claim Details ---");
                    for (Claim c : approvedClaims) {
                        System.out.println("Claim ID: " + c.getId()
                                + " | Insured: " + c.getInsuredPersonId()
                                + " | Amount: " + String.format("%,.2f VND", c.getClaimAmount())
                                + " | Claim Date: " + c.getClaimDate()
                                + " | Processed By: " + (c.getProcessedByUserId() != null ? c.getProcessedByUserId() : "Unassigned"));
                    }
                } else {
                    // An empty window is legitimate (no claims reached DONE in that
                    // period), but a bare "0" reads like a broken report. Spell out
                    // why it is empty and show the newest approved claim on file so
                    // the operator can see the dataset is populated.
                    System.out.println("No claims reached status DONE within " + windowLabel + ".");
                    LocalDateTime latest = null;
                    for (Claim c : context.getClaimRepository().getAll()) {
                        if (c.getStatus() == ClaimStatus.DONE
                                && (latest == null || c.getClaimDate().isAfter(latest))) {
                            latest = c.getClaimDate();
                        }
                    }
                    if (latest != null) {
                        System.out.println("Most recent approved (DONE) claim on file is dated: " + latest);
                        System.out.println("Try option 4 (Custom Date Range) to cover that period.");
                    } else {
                        System.out.println("No approved (DONE) claims exist in the system yet.");
                    }
                }
            }
        }
    }

    private static void viewPayoutPerOfficerReport(AppContext context) {
        System.out.println("\n===== TOTAL CLAIM PAYOUT PROCESSED PER CLAIMS OFFICER & STAFF =====");
        List<User> allUsers = context.getUserRepository().getAll();

        for (User u : allUsers) {
            if (u.getRole() == UserRole.OFFICER || u.getRole() == UserRole.ADMIN) {
                List<Claim> staffClaims = context.getClaimRepository().getApprovedClaimsByOfficer(u.getUserId());
                if (!staffClaims.isEmpty()) {
                    double staffPayout = context.getClaimRepository().getApprovedPayoutByOfficer(u.getUserId());
                    String roleLabel = (u.getRole() == UserRole.ADMIN) ? "Admin" : "Officer";
                    System.out.println("\n" + roleLabel + " : " + u.getFullName() + " (ID: " + u.getUserId() + ")");
                    System.out.println("Approved Claims Processed: " + staffClaims.size());
                    System.out.println("Total Approved Payout    : " + String.format("%,.2f VND", staffPayout));
                    System.out.println("Processed Claim IDs      : ");
                    for (Claim oc : staffClaims) {
                        System.out.println("  -> Claim " + oc.getId() + " | Amount: " + String.format("%,.2f VND", oc.getClaimAmount())
                                + " | Insured: " + oc.getInsuredPersonId() + " | Date: " + oc.getClaimDate());
                    }
                }
            }
        }

        double grandTotalApprovedPayout = 0.0;
        int grandTotalApprovedCount = 0;
        double unassignedPayout = 0.0;
        int unassignedCount = 0;

        for (Claim c : context.getClaimRepository().getAll()) {
            if (c.getStatus() == ClaimStatus.DONE) {
                grandTotalApprovedPayout += c.getClaimAmount();
                grandTotalApprovedCount++;
                if (c.getProcessedByUserId() == null || c.getProcessedByUserId().trim().isEmpty()) {
                    unassignedPayout += c.getClaimAmount();
                    unassignedCount++;
                }
            }
        }

        if (unassignedCount > 0) {
            System.out.println("\nLegacy / Unassigned Approved Claims (pre-existing before Phase 7 tracking):");
            System.out.println("Count: " + unassignedCount + " claim(s) | Payout: " + String.format("%,.2f VND", unassignedPayout));
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("Grand Total Approved Payout (All " + grandTotalApprovedCount + " Claims): "
                + String.format("%,.2f VND", grandTotalApprovedPayout));
        System.out.println("--------------------------------------------------------------------------------");
    }
}
