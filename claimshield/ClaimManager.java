package claimshield;
/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
/**
 * Manages the core data operations for the ClaimShield system,
 * including CRUD operations, validation, and file persistence
 * for customers, insurance cards, and claims.
 */

public class ClaimManager {
    private ArrayList<Customer> customers;
    private ArrayList<InsuranceCard> cards;
    private ArrayList<Claim> claims;

    public ClaimManager() {
        this.customers = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.claims = new ArrayList<>();
    }

    /**
     * Finds a customer by their unique ID.
     *
     * @param id the customer ID to search for
     * @return the matching Customer, or null if not found
     */
    public Customer getCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }

        return null;
    }

    /**
     * Finds an insurance card by its card number.
     *
     * @param cardNumber the card number to search for
     * @return the matching InsuranceCard, or null if not found
     */
    public InsuranceCard getCardByNumber(String cardNumber) {
        for (InsuranceCard card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Finds a claim by its unique ID.
     *
     * @param id the claim ID to search for
     * @return the matching Claim, or null if not found
     */
    public Claim getClaimById(String id) {
        for (Claim claim : claims) {
            if (claim.getId().equals(id)) {
                return claim;
            }
        }
        return null;
    }

    /**
     * Adds a new customer after validating the ID format, customer type,
     * and parent-child relationship for Dependents.
     *
     * @param customer the Customer to add
     * @return true if added successfully, false if validation fails
     */
    public boolean addCustomer(Customer customer) {
        if (customer == null) {
            return false;
        }
        if (!Validator.isValidCustomerId(customer.getId())) {
            return false;
        }
        if (getCustomerById(customer.getId()) != null) {
            return false;
        }
        if (!Validator.isValidCustomerType(customer.getCustomerType())) {
            return false;
        }
        if (customer.getCustomerType().equals("PolicyHolder") && customer.getParentPolicyHolderId() != null) {
            return false;
        }
        if (customer.getCustomerType().equals("Dependent")) {
            Customer parent = getCustomerById(customer.getParentPolicyHolderId());
            if (!Validator.isPolicyHolder(parent)) {
                return false;
            }
        }
        customers.add(customer);
        return true;
    }

    /**
     * Registers a new insurance card after validating the card number,
     * confirming the card holder exists, and confirming the policy owner
     * is a PolicyHolder.
     *
     * @param card the InsuranceCard to register
     * @return true if added successfully, false if validation fails
     */
    public boolean addInsuranceCard(InsuranceCard card) {
        if (card == null) {
            return false;
        }
        if (!Validator.isValidCardNumber(card.getCardNumber())) {
            return false;
        }
        if (getCardByNumber(card.getCardNumber()) != null) {
            return false;
        }
        if (getCustomerById(card.getCardHolderId()) == null) {
            return false;
        }
        Customer owner = getCustomerById(card.getPolicyOwnerId());
        if (!Validator.isPolicyHolder(owner)) {
            return false;
        }
        cards.add(card);
        return true;
    }

    /**
     * Adds a new claim to the system after validating all attributes.
     *
     * Design Note: Returns boolean false for basic input/format or existence validations
     * (invalid ID format, duplicate ID, non-existent customer or card, non-positive amount, invalid status).
     * Throws InvalidClaimDateException for domain business rule violations
     * (exam date after claim date, or exam date on/after card expiration) to provide specific failure reasons.
     *
     * @param claim the Claim to add
     * @return true if added successfully, false if basic validation fails
     * @throws InvalidClaimDateException if exam date violates chronological or card expiration rules
     */
    public boolean addClaim(Claim claim) throws InvalidClaimDateException {
        if (claim == null) {
            return false;
        }
        if (!Validator.isValidClaimId(claim.getId())) {
            return false;
        }
        if (getClaimById(claim.getId()) != null) {
            return false;
        }
        if (getCustomerById(claim.getInsuredPersonId()) == null) {
            return false;
        }
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card == null) {
            return false;
        }
        if (!Validator.isPositiveAmount(claim.getClaimAmount())) {
            return false;
        }
        if (!Validator.isValidStatus(claim.getStatus())) {
            return false;
        }
        if (claim.getExamDate().toLocalDate()
                .isAfter(claim.getClaimDate().toLocalDate())) {
            throw new InvalidClaimDateException(
                    "Exam date (" + claim.getExamDate().toLocalDate()
                            + ") must be on or before claim date (" + claim.getClaimDate().toLocalDate() + ").");
        }
        if (!claim.getExamDate().isBefore(card.getExpirationDate())) {
            throw new InvalidClaimDateException(
                    "Exam date (" + claim.getExamDate()
                            + ") must be strictly before insurance card expiration date (" + card.getExpirationDate() + ").");
        }
        claims.add(claim);
        return true;
    }

    /**
     * Updates a claim's status, enforcing that status can only move
     * forward (NEW -> PROCESSING -> DONE), and is immutable once DONE.
     *
     * Design Note: Returns boolean false if the claim is not found or the new status is null/unrecognized.
     * Throws InvalidStatusTransitionException when an invalid or backward status transition is attempted
     * (business rule violation) to provide detailed contextual error information.
     *
     * @param claimId   the ID of the claim to update
     * @param newStatus the target status
     * @return true if updated successfully, false if claim not found or status is null
     * @throws InvalidStatusTransitionException if an invalid or backward status transition is attempted
     */
    public boolean updateClaimStatus(String claimId, ClaimStatus newStatus) throws InvalidStatusTransitionException {
        Claim claim = getClaimById(claimId);
        if (claim == null || newStatus == null) {
            return false;
        }
        if (claim.getStatus() == ClaimStatus.DONE) {
            throw new InvalidStatusTransitionException(
                    "Cannot update claim " + claimId + ": current status is DONE and cannot be modified.");
        }
        int currentRank = statusRank(claim.getStatus());
        int newRank = statusRank(newStatus);
        if (newRank <= currentRank) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition for claim " + claimId
                            + ": cannot transition from " + claim.getStatus() + " to " + newStatus
                            + ". Status must move forward (NEW -> PROCESSING -> DONE).");
        }
        claim.setStatus(newStatus);
        return true;
    }

    /**
     * Updates a claim's status using a string representation.
     *
     * Design Note: Returns boolean false if the status string is unrecognized.
     * Throws InvalidStatusTransitionException if the status transition violates business rules.
     *
     * @param claimId   the ID of the claim to update
     * @param newStatus the target status string
     * @return true if updated successfully, false if claim not found or status string is invalid
     * @throws InvalidStatusTransitionException if an invalid or backward status transition is attempted
     */
    public boolean updateClaimStatus(String claimId, String newStatus) throws InvalidStatusTransitionException {
        ClaimStatus status = ClaimStatus.fromString(newStatus);
        if (status == null) {
            return false;
        }
        return updateClaimStatus(claimId, status);
    }

    /**
     * Adds a document to a claim after validating the document name
     * follows the required ClaimId_CardNumber_Name.pdf format.
     *
     * @param claimId      the ID of the claim
     * @param documentName the document file name to add
     * @return true if added successfully, false if validation fails
     */
    public boolean addDocumentToClaim(String claimId, String documentName) {
        Claim claim = getClaimById(claimId);
        if (claim == null) {
            return false;
        }
        if (!Validator.isValidDocumentName(documentName, claim.getId(), claim.getCardNumber())) {
            return false;
        }
        claim.addDocument(documentName);
        return true;
    }

    private int statusRank(ClaimStatus status) {
        if (status == null) {
            return -1;
        }

        switch (status) {
            case NEW:
                return 0;
            case PROCESSING:
                return 1;
            case DONE:
                return 2;
            default:
                return -1;
        }
    }

    private int statusRank(String status) {
        return statusRank(ClaimStatus.fromString(status));
    }

    /**
     * Returns a copy of all customers in the system.
     *
     * @return an ArrayList containing all Customer records
     */
    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    /**
     * Returns a copy of all insurance cards in the system.
     *
     * @return an ArrayList containing all InsuranceCard records
     */
    public ArrayList<InsuranceCard> getAllCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Returns a copy of all claims in the system.
     *
     * @return an ArrayList containing all Claim records
     */
    public ArrayList<Claim> getAllClaims() {
        return new ArrayList<>(claims);
    }

    /**
     * Removes a customer from the system by ID.
     *
     * @param id the customer ID to remove
     * @return true if removed successfully, false if not found
     */
    public boolean deleteCustomer(String id) {
        Customer customer = getCustomerById(id);
        if (customer == null) {
            return false;
        }
        customers.remove(customer);
        return true;
    }

    /**
     * Removes an insurance card from the system by card number.
     *
     * @param cardNumber the card number to remove
     * @return true if removed successfully, false if not found
     */
    public boolean deleteInsuranceCard(String cardNumber) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) {
            return false;
        }
        cards.remove(card);
        return true;
    }

    /**
     * Removes a claim from the system by ID.
     *
     * @param id the claim ID to remove
     * @return true if removed successfully, false if not found
     */
    public boolean deleteClaim(String id) {
        Claim claim = getClaimById(id);
        if (claim == null) {
            return false;
        }
        claims.remove(claim);
        return true;
    }

    /**
     * Saves all customers to the specified file in CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveCustomersToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Customer customer : customers) {
                writer.println(customer.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving customers: " + e.getMessage());
        }
    }

    /**
     * Saves all insurance cards to the specified file in CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveCardsToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (InsuranceCard card : cards) {
                writer.println(card.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving cards: " + e.getMessage());
        }
    }

    /**
     * Loads customers from the specified file, skipping and reporting
     * any invalid or malformed lines without stopping the program.
     *
     * @param filePath the source file path
     */
    public void loadCustomersFromFile(String filePath) {
        customers.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length != 4) {
                    System.out.println("Skipping invalid customer line: " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String fullName = parts[1].trim();
                    String customerType = parts[2].trim();
                    String parentId = parts[3].trim().equals("null")
                            ? null
                            : parts[3].trim();

                    Customer customer = null;
                    if ("PolicyHolder".equalsIgnoreCase(customerType)) {
                        customer = new PolicyHolder(null, null, null, fullName, null, null, id);
                    } else if ("Dependent".equalsIgnoreCase(customerType)) {
                        customer = new Dependent(null, null, null, fullName, null, null, id, parentId);
                    }

                    if (customer == null || !addCustomer(customer)) {
                        System.out.println("Skipping invalid customer line: " + line);
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Skipping invalid customer line: "
                                    + line
                                    + " ("
                                    + e.getMessage()
                                    + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }

    /**
     * Loads insurance cards from the specified file, skipping and reporting
     * any invalid or malformed lines without stopping the program.
     *
     * @param filePath the source file path
     */
    public void loadCardsFromFile(String filePath) {
        cards.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length != 4) {
                    System.out.println("Skipping invalid card line: " + line);
                    continue;
                }

                try {
                    String cardNumber = parts[0].trim();
                    String cardHolderId = parts[1].trim();
                    String policyOwnerId = parts[2].trim();
                    LocalDateTime expirationDate = LocalDateTime.parse(parts[3].trim());

                    InsuranceCard card = new InsuranceCard(
                            cardNumber,
                            cardHolderId,
                            policyOwnerId,
                            expirationDate);

                    if (!addInsuranceCard(card)) {
                        System.out.println("Skipping invalid card line: " + line);
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Skipping invalid card line: "
                                    + line
                                    + " ("
                                    + e.getMessage()
                                    + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading cards: " + e.getMessage());
        }
    }

    /**
     * Saves all claims to the specified file in CSV format.
     *
     * @param filePath the destination file path
     */
    public void saveClaimsToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Claim claim : claims) {
                writer.println(claim.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving claims: " + e.getMessage());
        }
    }

    /**
     * Loads claims from the specified file, skipping and reporting
     * any invalid or malformed lines without stopping the program.
     *
     * @param filePath the source file path
     */
    public void loadClaimsFromFile(String filePath) {
        claims.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length < 7) {
                    System.out.println("Skipping invalid claim line: " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    LocalDateTime claimDate = LocalDateTime.parse(parts[1].trim());
                    String insuredPersonId = parts[2].trim();
                    String cardNumber = parts[3].trim();
                    LocalDateTime examDate = LocalDateTime.parse(parts[4].trim());
                    double claimAmount = Double.parseDouble(parts[5].trim());
                    String statusStr = parts[6].trim();
                    ClaimStatus status = ClaimStatus.fromString(statusStr);
                    if (status == null) {
                        System.out.println("Skipping invalid claim line: " + line);
                        continue;
                    }

                    Claim claim = new Claim(
                            id,
                            claimDate,
                            insuredPersonId,
                            cardNumber,
                            examDate,
                            claimAmount,
                            status);

                    if (!addClaim(claim)) {
                        System.out.println("Skipping invalid claim line: " + line);
                        continue;
                    }

                    if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                        String[] documents = parts[7].trim().split("\\|");

                        for (String document : documents) {
                            String documentName = document.trim();

                            if (Validator.isValidDocumentName(
                                    documentName,
                                    claim.getId(),
                                    claim.getCardNumber())) {
                                claim.addDocument(documentName);
                            } else {
                                System.out.println("Skipping invalid document: " + documentName);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Skipping invalid claim line: "
                                    + line
                                    + " ("
                                    + e.getMessage()
                                    + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading claims: " + e.getMessage());
        }
    }
}
