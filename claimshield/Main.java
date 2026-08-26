package claimshield;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Console-based entry point for the ClaimShield system, providing
 * a menu-driven interface for administrators to manage customers,
 * insurance cards, and claims.
 */
public class Main {
    public static void main(String[] args) {
        ClaimManager manager = new ClaimManager();
        Scanner sc = new Scanner(System.in);

        manager.loadCustomersFromFile("data/customers.txt");
        manager.loadCardsFromFile("data/cards.txt");
        manager.loadClaimsFromFile("data/claims.txt");
        System.out.println("Loaded " + manager.getAllCustomers().size() + " customers, "
                + manager.getAllCards().size() + " cards, "
                + manager.getAllClaims().size() + " claims.");

        boolean running = true;
        while (running) {
            System.out.println("\n===== ClaimShield Main Menu =====");
            System.out.println("1. Manage Customer Directory");
            System.out.println("2. Manage Insurance Cards");
            System.out.println("3. Process Claims");
            System.out.println("4. Save and Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    customerMenu(manager, sc);
                    break;
                case "2":
                    cardMenu(manager, sc);
                    break;
                case "3":
                    claimMenu(manager, sc);
                    break;
                case "4":
                    manager.saveCustomersToFile("data/customers.txt");
                    manager.saveCardsToFile("data/cards.txt");
                    manager.saveClaimsToFile("data/claims.txt");
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        sc.close();
    }

    // Displays the customer management submenu and routes user choices.
    private static void customerMenu(ClaimManager manager, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Customer Menu -----");
            System.out.println("1. Add Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Remove Customer");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    addCustomerFlow(manager, sc);
                    break;
                case "2":
                    viewAllCustomers(manager);
                    break;
                case "3":
                    removeCustomerFlow(manager, sc);
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Prints all customers currently in the system.
    private static void viewAllCustomers(ClaimManager manager) {
        System.out.println("\n----- All Customers -----");
        if (manager.getAllCustomers().isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : manager.getAllCustomers()) {
            System.out.println(c);
        }
    }

    // Prompts for customer details and attempts to add a new customer.
    private static void addCustomerFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Add Customer -----");

        System.out.print("Customer ID (format c-1234567): ");
        String id = sc.nextLine().trim();

        System.out.print("Full Name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Customer Type (PolicyHolder / Dependent): ");
        String customerType = sc.nextLine().trim();

        String parentId = null;
        if (customerType.equals("Dependent")) {
            System.out.print("Parent PolicyHolder ID: ");
            parentId = sc.nextLine().trim();
        }

        Customer customer = null;
        if ("PolicyHolder".equalsIgnoreCase(customerType)) {
            customer = new PolicyHolder(null, null, null, fullName, null, null, id);
        } else if ("Dependent".equalsIgnoreCase(customerType)) {
            customer = new Dependent(null, null, null, fullName, null, null, id, parentId);
        }
        boolean success = customer != null && manager.addCustomer(customer);

        if (success) {
            System.out.println("Customer added successfully.");
        } else {
            System.out.println("Failed to add customer. Please check ID format, customer type, and parent reference.");
        }
    }

    // Prompts for a customer ID and attempts to remove that customer.
    private static void removeCustomerFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Remove Customer -----");
        System.out.print("Enter Customer ID to remove: ");
        String id = sc.nextLine().trim();

        boolean success = manager.deleteCustomer(id);
        if (success) {
            System.out.println("Customer removed successfully.");
        } else {
            System.out.println("Customer not found.");
        }
    }

    // Displays the insurance card management submenu and routes user choices.
    private static void cardMenu(ClaimManager manager, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Insurance Card Menu -----");
            System.out.println("1. Register New Card");
            System.out.println("2. View All Cards");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    addCardFlow(manager, sc);
                    break;
                case "2":
                    viewAllCards(manager);
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Prints all insurance cards currently in the system.
    private static void viewAllCards(ClaimManager manager) {
        System.out.println("\n----- All Insurance Cards -----");
        if (manager.getAllCards().isEmpty()) {
            System.out.println("No cards found.");
            return;
        }
        for (InsuranceCard c : manager.getAllCards()) {
            System.out.println(c);
        }
    }

    // Prompts for card details and attempts to register a new insurance card.
    private static void addCardFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Register New Card -----");

        System.out.print("Card Number (10 digits): ");
        String cardNumber = sc.nextLine().trim();

        System.out.print("Card Holder ID: ");
        String cardHolderId = sc.nextLine().trim();

        System.out.print("Policy Owner ID: ");
        String policyOwnerId = sc.nextLine().trim();

        System.out.print("Expiration Date (yyyy-MM-ddTHH:mm, e.g. 2028-12-31T00:00): ");
        String dateInput = sc.nextLine().trim();

        LocalDateTime expirationDate;
        try {
            expirationDate = LocalDateTime.parse(dateInput);
        } catch (Exception e) {
            System.out.println("Invalid date format. Card not added.");
            return;
        }

        InsuranceCard card = new InsuranceCard(cardNumber, cardHolderId, policyOwnerId, expirationDate);
        boolean success = manager.addInsuranceCard(card);

        if (success) {
            System.out.println("Card registered successfully.");
        } else {
            System.out.println("Failed to register card. Please check card number, holder ID, and owner ID.");
        }
    }

    // Displays the claim management submenu and routes user choices.
    private static void claimMenu(ClaimManager manager, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Claim Menu -----");
            System.out.println("1. Create New Claim");
            System.out.println("2. Add Document to Claim");
            System.out.println("3. Update Claim Status");
            System.out.println("4. View All Claims");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    addClaimFlow(manager, sc);
                    break;
                case "2":
                    addDocumentFlow(manager, sc);
                    break;
                case "3":
                    updateStatusFlow(manager, sc);
                    break;
                case "4":
                    viewAllClaims(manager);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Prints all claims currently in the system.
    private static void viewAllClaims(ClaimManager manager) {
        System.out.println("\n----- All Claims -----");
        if (manager.getAllClaims().isEmpty()) {
            System.out.println("No claims found.");
            return;
        }
        for (Claim c : manager.getAllClaims()) {
            System.out.println(c);
        }
    }

    // Prompts for claim details and attempts to create a new claim with status
    // "New".
    private static void addClaimFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Create New Claim -----");

        System.out.print("Claim ID (format f-1234567890): ");
        String id = sc.nextLine().trim();

        System.out.print("Claim Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime claimDate;
        try {
            claimDate = LocalDateTime.parse(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid claim date format. Claim not created.");
            return;
        }

        System.out.print("Insured Person ID: ");
        String insuredPersonId = sc.nextLine().trim();

        System.out.print("Card Number: ");
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
            System.out.println("Invalid amount. Claim not created.");
            return;
        }

        Claim claim = new Claim(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount, ClaimStatus.NEW);
        try {
            boolean success = manager.addClaim(claim);
            if (success) {
                System.out.println("Claim created successfully with status NEW.");
            } else {
                System.out.println("Failed to create claim. Please check ID format, duplicate ID, references, and amount.");
            }
        } catch (InvalidClaimDateException e) {
            System.out.println("Error creating claim: " + e.getMessage());
        }
    }

    // Prompts for a claim ID and document name, then attempts to add the document.
    private static void addDocumentFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Add Document to Claim -----");

        System.out.print("Claim ID: ");
        String claimId = sc.nextLine().trim();

        System.out.print("Document Name (format ClaimId_CardNumber_Name.pdf): ");
        String documentName = sc.nextLine().trim();

        boolean success = manager.addDocumentToClaim(claimId, documentName);
        if (success) {
            System.out.println("Document added successfully.");
        } else {
            System.out.println("Failed to add document. Please check claim ID and document name format.");
        }
    }

    // Prompts for a claim ID and new status, then attempts to update the claim's
    // status.
    private static void updateStatusFlow(ClaimManager manager, Scanner sc) {
        System.out.println("\n----- Update Claim Status -----");

        System.out.print("Claim ID: ");
        String claimId = sc.nextLine().trim();

        System.out.print("New Status (NEW / PROCESSING / DONE): ");
        String newStatus = sc.nextLine().trim();

        try {
            boolean success = manager.updateClaimStatus(claimId, newStatus);
            if (success) {
                System.out.println("Claim status updated successfully.");
            } else {
                System.out.println("Failed to update status. Claim not found or unrecognized status.");
            }
        } catch (InvalidStatusTransitionException e) {
            System.out.println("Error updating status: " + e.getMessage());
        }
    }
}
