package claimshield;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Console operations focused on insurance-card registration, queries, updates, and safe removal.
 */
final class CardConsole {
    private CardConsole() {
    }

    static void adminCardSubMenu(AppContext context, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Admin: Insurance Card Management -----");
            System.out.println("1. Register New Card");
            System.out.println("2. View All Cards");
            System.out.println("3. View Card by Card Number");
            System.out.println("4. Find Card by Card Holder ID");
            System.out.println("5. View Cards Funded by a Policy Owner");
            System.out.println("6. View Cards Expiring Before a Date");
            System.out.println("7. Update Insurance Card Details");
            System.out.println("8. Remove Card");
            System.out.println("9. Back to Admin Menu");
            System.out.print("Choose an option: ");
            String choice = ConsoleSupport.readLine(sc).trim();

            switch (choice) {
                case "1":
                    addCardFlow(context, sc);
                    break;
                case "2":
                    viewAllCards(context);
                    break;
                case "3":
                    viewCardByNumberFlow(context, sc);
                    break;
                case "4":
                    findCardByHolderFlow(context, sc);
                    break;
                case "5":
                    viewCardsByPolicyOwnerFlow(context, sc);
                    break;
                case "6":
                    viewCardsExpiringBeforeFlow(context, sc);
                    break;
                case "7":
                    adminUpdateCardFlow(context, sc);
                    break;
                case "8":
                    removeCardFlow(context, sc);
                    break;
                case "9":
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
        String cardNumber = ConsoleSupport.readLine(sc).trim();

        System.out.print("Card Holder Customer ID: ");
        String cardHolderId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Policy Owner Customer ID: ");
        String policyOwnerId = ConsoleSupport.readLine(sc).trim();

        System.out.print("Expiration Date (yyyy-MM-ddTHH:mm): ");
        LocalDateTime expirationDate;
        try {
            expirationDate = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
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
            context.autoSave();
            System.out.println("Insurance card registered and linked successfully. (Data auto-saved to files)");
        } else {
            System.out.println("Failed to register card. Check 10-digit format and ensure card number is unique.");
        }
    }

    static void viewAllCards(AppContext context) {
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

    private static void viewCardByNumberFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- View Insurance Card by Card Number ---");
        System.out.print("Enter Card Number: ");
        String cardNumber = ConsoleSupport.readLine(sc).trim();

        InsuranceCard card = context.getCardRepository().getById(cardNumber);
        if (card != null) {
            System.out.println(card);
        } else {
            System.out.println("Card not found with number: " + cardNumber);
        }
    }

    private static void removeCardFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Remove Insurance Card ---");
        System.out.print("Enter Card Number to remove: ");
        String cardNumber = ConsoleSupport.readLine(sc).trim();

        InsuranceCard card = context.getCardRepository().getById(cardNumber);
        if (card == null) {
            System.out.println("Card not found with number: " + cardNumber);
            return;
        }
        if (context.getCardRepository().hasClaimsForCard(cardNumber)) {
            System.out.println("Cannot remove card " + cardNumber
                    + ": it still has associated claim(s). Remove or reassign those claims before deleting the card.");
            return;
        }

        boolean success = context.getCardRepository().delete(cardNumber);
        if (success) {
            context.autoSave();
            System.out.println("Insurance card removed successfully. (Data auto-saved to files)");
        } else {
            System.out.println("Failed to remove card with number: " + cardNumber);
        }
    }


    private static void findCardByHolderFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Find Insurance Card by Card Holder ---");
        System.out.print("Enter Card Holder Customer ID (c-XXXXXXX): ");
        String holderId = ConsoleSupport.readLine(sc).trim();

        InsuranceCard card = context.getCardRepository().getByCardHolderId(holderId);
        if (card != null) {
            System.out.println(card);
        } else {
            System.out.println("No insurance card issued to customer ID: " + holderId);
        }
    }

    /**
     * Admin flow for the CardManageable policy-owner filter, which returns the
     * whole family plan funded by one PolicyHolder.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void viewCardsByPolicyOwnerFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Insurance Cards Funded by a Policy Owner ---");
        System.out.print("Enter Policy Owner Customer ID (c-XXXXXXX): ");
        String ownerId = ConsoleSupport.readLine(sc).trim();

        List<InsuranceCard> cards = context.getCardRepository().getByPolicyOwnerId(ownerId);
        if (cards.isEmpty()) {
            System.out.println("No insurance cards funded by policy owner: " + ownerId);
            return;
        }
        System.out.println("Found " + cards.size() + " card(s) funded by " + ownerId + ":");
        for (InsuranceCard card : cards) {
            System.out.println(card);
        }
    }

    /**
     * Admin flow for the CardManageable expiry filter, used for renewal reporting.
     *
     * @param context the shared application context
     * @param sc      the active console scanner
     */
    private static void viewCardsExpiringBeforeFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Insurance Cards Expiring Before a Date ---");
        System.out.print("Enter deadline (yyyy-MM-ddTHH:mm): ");
        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(ConsoleSupport.readLine(sc).trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date-time format (expected yyyy-MM-ddTHH:mm).");
            return;
        }

        List<InsuranceCard> cards = context.getCardRepository().filterExpiringBefore(deadline);
        if (cards.isEmpty()) {
            System.out.println("No insurance cards expire before " + deadline + ".");
            return;
        }
        System.out.println("Found " + cards.size() + " card(s) expiring before " + deadline + ":");
        for (InsuranceCard card : cards) {
            System.out.println(card);
        }
    }

    private static void adminUpdateCardFlow(AppContext context, Scanner sc) {
        System.out.println("\n--- Update Insurance Card Details ---");
        System.out.print("Card Number to update: ");
        String cardNumber = ConsoleSupport.readLine(sc).trim();

        InsuranceCard card = context.getCardRepository().getById(cardNumber);
        if (card == null) {
            System.out.println("Card not found with number: " + cardNumber);
            return;
        }

        System.out.println("Editing card: " + card.getCardNumber()
                + " (Holder: " + card.getCardHolderId() + ")");
        System.out.println("Leave a field blank and press Enter to keep its current value.");

        System.out.print("New Expiration Date [" + card.getExpirationDate() + "]: ");
        String expInput = ConsoleSupport.readLine(sc).trim();
        if (!expInput.isEmpty()) {
            try {
                LocalDateTime newExp = LocalDateTime.parse(expInput);
                List<Claim> cardClaims = context.getClaimRepository().filterByCardNumber(card.getCardNumber());
                boolean valid = true;
                for (Claim cl : cardClaims) {
                    if (!cl.getExamDate().isBefore(newExp)) {
                        System.out.println("Invalid expiration date: Claim " + cl.getId()
                                + " has exam date " + cl.getExamDate()
                                + " which is not strictly before proposed expiration date " + newExp
                                + ". Keeping current expiration date.");
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    card.setExpirationDate(newExp);
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date-time format (expected yyyy-MM-ddTHH:mm). Keeping the current value.");
            }
        }

        System.out.print("New Policy Owner Customer ID [" + card.getPolicyOwnerId() + "]: ");
        String ownerId = ConsoleSupport.readLine(sc).trim();
        if (!ownerId.isEmpty()) {
            if (!Validator.isValidCustomerId(ownerId)) {
                System.out.println("Invalid customer ID format. Keeping the current value.");
            } else if (context.getCustomerRepository().getById(ownerId) == null) {
                System.out.println("No customer found with ID " + ownerId + ". Keeping the current value.");
            } else {
                card.setPolicyOwnerId(ownerId);
            }
        }

        context.getCardRepository().update(card);
        context.autoSave();
        System.out.println("Insurance card " + card.getCardNumber() + " updated successfully. (Data auto-saved to files)");
    }


}
