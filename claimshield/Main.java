package claimshield;

import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Application entry point. This class owns startup, authentication, role
 * routing, and graceful shutdown only; role-specific menu behaviour lives in
 * {@link AdminMenu}, {@link OfficerMenu}, and {@link CustomerPortal}.
 */
public class Main {
    private static AppContext appContext;

    /**
     * Starts ClaimShield, loads persisted data, authenticates users, and routes
     * each successful session to the corresponding role menu.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printBanner();

        AppContext context = new AppContext();
        appContext = context;
        context.loadAll(
                ConsoleSupport.USERS_FILE,
                ConsoleSupport.CUSTOMERS_FILE,
                ConsoleSupport.CARDS_FILE,
                ConsoleSupport.CLAIMS_FILE,
                ConsoleSupport.LOGS_FILE);
        printLoadSummary(context);

        try (Scanner sc = new Scanner(System.in)) {
            runLoginLoop(context, sc);
        } catch (ConsoleSupport.EndOfInputException e) {
            saveAndPrintGoodbye();
        }
    }

    /** Prints the standardized assignment banner. */
    private static void printBanner() {
        System.out.println("=======================================");
        System.out.println("COSC3110/3111 HEALTH INSURANCE SYSTEM");
        System.out.println("                        Student ID: S4113887");
        System.out.println("                    Student Name: Nguyen Ngoc Quang Dang");
        System.out.println("=======================================");
    }

    /** Prints the number of records loaded into each repository. */
    private static void printLoadSummary(AppContext context) {
        System.out.println("\nSystem loaded: "
                + context.getUserRepository().getAll().size() + " users, "
                + context.getCustomerRepository().getAll().size() + " customers, "
                + context.getCardRepository().getAll().size() + " cards, "
                + context.getClaimRepository().getAll().size() + " claims, "
                + context.getAuditTrail().size() + " audit log entries.");
    }

    /** Runs repeated login sessions until the operator enters "exit". */
    private static void runLoginLoop(AppContext context, Scanner sc) {
        boolean appRunning = true;
        while (appRunning) {
            System.out.println("\n===== LOGIN SYSTEM =====");
            System.out.print("Username (or 'exit' to quit): ");
            String username = ConsoleSupport.readLine(sc).trim();

            if ("exit".equalsIgnoreCase(username)) {
                saveAndPrintGoodbye();
                appRunning = false;
                continue;
            }

            System.out.print("Password: ");
            String password = ConsoleSupport.readLine(sc);
            User currentUser = context.getUserRepository().authenticate(username, password);
            if (currentUser == null) {
                AuditLogger.log("UNKNOWN", "LOGIN_FAILED", username);
                System.out.println(
                        "Login failed! Invalid username, password, or account is inactive. Please try again.");
                continue;
            }

            runUserSession(context, currentUser, sc);
        }
    }

    /** Establishes the audit context and dispatches a role-specific session. */
    private static void runUserSession(AppContext context, User currentUser, Scanner sc) {
        AppContext.setCurrentSessionUser(currentUser);
        AuditLogger.log(currentUser.getUserId(), "LOGIN_SUCCESS", currentUser.getUserId());
        currentUser.displayDashboard();

        switch (currentUser.getRole()) {
            case ADMIN:
                AdminMenu.show(context, (Admin) currentUser, sc);
                break;
            case OFFICER:
                OfficerMenu.show(context, (ClaimsOfficer) currentUser, sc);
                break;
            case CUSTOMER:
                CustomerPortal.show(context, (Customer) currentUser, sc);
                break;
            default:
                System.out.println("Unknown user role encountered.");
        }

        AuditLogger.log(currentUser.getUserId(), "LOGOUT", currentUser.getUserId());
        AppContext.setCurrentSessionUser(null);
    }

    /** Saves every dataset and prints the standardized shutdown message. */
    private static void saveAndPrintGoodbye() {
        System.out.println("\nSaving all data before exit...");
        if (appContext != null) {
            appContext.saveAll(
                    ConsoleSupport.USERS_FILE,
                    ConsoleSupport.CUSTOMERS_FILE,
                    ConsoleSupport.CARDS_FILE,
                    ConsoleSupport.CLAIMS_FILE);
        }
        System.out.println("All data saved successfully. Goodbye!");
    }
}
