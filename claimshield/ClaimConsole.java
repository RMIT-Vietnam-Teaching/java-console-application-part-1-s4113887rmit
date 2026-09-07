package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Console operations focused on claim creation, processing, filtering, display, and removal.
 */
final class ClaimConsole {
    private ClaimConsole() {
    }

    static void addClaimFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Create New Claim ---");
        System.out.print("Claim ID (format f-XXXXXXXXXX): ");
        String id = ConsoleSupport.readLine(sc).trim();

        System.out.print("Claim Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime claimDate;
        try {
            claimDate = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
        } catch (Exception e) {
            System.out.println("Invalid claim date format. Claim not created.");
            return;
        }

        System.out.print("Insured Person Customer ID: ");
        String insuredPersonId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Card Number (10 digits): ");
        String cardNumber = ConsoleSupport.readLine(sc).trim();

        System.out.print("Exam Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime examDate;
        try {
            examDate = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
        } catch (Exception e) {
            System.out.println("Invalid exam date format. Claim not created.");
            return;
        }

        System.out.print("Claim Amount: ");
        double claimAmount;
        try {
            claimAmount = Double.parseDouble(ConsoleSupport.readLine(sc).trim());
        } catch (Exception e) {
            System.out.println("Invalid amount format. Claim not created.");
            return;
        }

        Claim claim = new Claim(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount, ClaimStatus.NEW);
        try {
            boolean success = context.getClaimRepository().add(claim);
            if (success) {
                context.autoSave();
                System.out.println("Claim created successfully with status NEW. (Data auto-saved to files)");
            } else {
                System.out.println(
                        "Failed to create claim. Please check ID format, duplicate ID, references, card ownership, and positive amount.");
            }
        } catch (InvalidClaimDateException e) {
            System.out.println("Business Rule Violation (Invalid Claim Date): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error creating claim: " + e.getMessage());
        }
    }

    static void addDocumentFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Add Supporting Document to Claim ---");
        System.out.print("Claim ID: ");
        String claimId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Document Name (format ClaimId_CardNumber_Name.pdf): ");
        String documentName = ConsoleSupport.readLine(sc).trim();

        try {
            boolean success = context.getClaimRepository().addDocument(claimId, documentName);
            if (success) {
                context.autoSave();
                System.out.println("Document added successfully to claim " + claimId + ". (Data auto-saved to files)");
            } else {
                System.out.println("Failed to add document. Claim not found with ID: " + claimId);
            }
        } catch (InvalidStatusTransitionException e) {
            System.out.println("Business Rule Violation (Claim Immutable): " + e.getMessage());
        } catch (InvalidDocumentNameException e) {
            System.out.println("Business Rule Violation (Invalid Document Name): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error adding document: " + e.getMessage());
        }
    }

    static void updateClaimStatusFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Update Claim Status ---");
        System.out.print("Claim ID: ");
        String claimId = ConsoleSupport.readLine(sc).trim();

        System.out.print("New Status (NEW / PROCESSING / DONE): ");
        String newStatusStr = ConsoleSupport.readLine(sc).trim();

        ClaimStatus newStatus = ClaimStatus.fromString(newStatusStr);
        if (newStatus == null) {
            System.out.println("Unrecognized status: '" + newStatusStr + "'. Allowed values: NEW, PROCESSING, DONE.");
            return;
        }

        try {
            boolean success = context.getClaimRepository().updateClaimStatus(claimId, newStatus);
            if (success) {
                context.autoSave();
                System.out.println("Claim status updated successfully to " + newStatus + ". (Data auto-saved to files)");
            } else {
                System.out.println("Failed to update status. Claim not found with ID: " + claimId);
            }
        } catch (InvalidStatusTransitionException e) {
            System.out.println("Business Rule Violation (Invalid Status Transition): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating claim status: " + e.getMessage());
        }
    }

    static void viewAllClaims(AppContext context) {
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

    static void viewClaimByIdFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- View Claim by ID ---");
        System.out.print("Enter Claim ID: ");
        String claimId = ConsoleSupport.readLine(sc).trim();

        Claim claim = context.getClaimRepository().getById(claimId);
        if (claim != null) {
            System.out.println(claim);
        } else {
            System.out.println("Claim not found with ID: " + claimId);
        }
    }

    static void filterClaimsByStatusFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by Status ---");
        System.out.print("Status to filter (NEW / PROCESSING / DONE): ");
        String statusStr = ConsoleSupport.readLine(sc).trim();
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

    static void filterClaimsByDateRangeFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by Date Range ---");
        System.out.print("Start Date-Time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
        } catch (Exception e) {
            System.out.println("Invalid start date format.");
            return;
        }

        System.out.print("End Date-Time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end;
        try {
            end = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
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

    static void filterClaimsByFamilyFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Filter Claims by PolicyHolder Family ---");
        System.out.print("Enter PolicyHolder Customer ID (c-XXXXXXX): ");
        String policyHolderId = ConsoleSupport.readLine(sc).trim();

        List<Claim> filtered = context.getClaimRepository().filterByPolicyHolderFamily(policyHolderId);
        System.out.println("Found " + filtered.size() + " claim(s) for family of PolicyHolder " + policyHolderId + ":");
        for (Claim c : filtered) {
            System.out.println(c);
        }
    }

    static void removeClaimFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Remove Claim ---");
        System.out.print("Enter Claim ID to remove: ");
        String claimId = ConsoleSupport.readLine(sc).trim();

        boolean success = context.getClaimRepository().delete(claimId);
        if (success) {
            context.autoSave();
            System.out.println("Claim removed successfully. (Data auto-saved to files)");
        } else {
            System.out.println("Claim not found with ID: " + claimId);
        }
    }
}
