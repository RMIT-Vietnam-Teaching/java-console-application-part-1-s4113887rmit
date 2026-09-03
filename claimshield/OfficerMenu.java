package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Role-specific menu coordinator for claims-officer sessions.
 */
final class OfficerMenu {
    private OfficerMenu() {
    }

    static void show(AppContext context, ClaimsOfficer officer, Scanner sc) {
        // Record the start of this claims officer session in the audit trail
        AuditLogger.log(officer.getUserId(), "OFFICER_SESSION_START", officer.getUserId());
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
            System.out.println("9. View Claim by ID");
            System.out.println("10. Save All Changes to Files");
            System.out.println("11. Logout to Login Screen");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    ClaimConsole.updateClaimStatusFlow(context, sc);
                    break;
                case "2":
                    ClaimConsole.addDocumentFlow(context, sc);
                    break;
                case "3":
                    ClaimConsole.viewAllClaims(context);
                    break;
                case "4":
                    ClaimConsole.filterClaimsByStatusFlow(context, sc);
                    break;
                case "5":
                    ClaimConsole.filterClaimsByDateRangeFlow(context, sc);
                    break;
                case "6":
                    ClaimConsole.filterClaimsByFamilyFlow(context, sc);
                    break;
                case "7":
                    CustomerConsole.adminViewAllCustomers(context);
                    break;
                case "8":
                    CardConsole.viewAllCards(context);
                    break;
                case "9":
                    ClaimConsole.viewClaimByIdFlow(context, sc);
                    break;
                case "10":
                    context.saveAll(ConsoleSupport.USERS_FILE, ConsoleSupport.CUSTOMERS_FILE, ConsoleSupport.CARDS_FILE, ConsoleSupport.CLAIMS_FILE);
                    System.out.println("All system datasets saved successfully to files.");
                    break;
                case "11":
                    System.out.println("Logging out from Claims Officer session...");
                    inSession = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 11.");
            }
        }
    }

    // =========================================================================
    // CUSTOMER MENU & OPERATIONS (STRICTLY READ-ONLY)
    // =========================================================================


}
