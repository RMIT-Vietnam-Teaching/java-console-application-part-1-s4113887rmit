package claimshield;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
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
    public Customer getCustomerById(String id) {
    for (Customer c : customers) {
        if (c.getId().equals(id)) {
            return c;
        }
    }
    return null;
    }

    public InsuranceCard getCardByNumber(String cardNumber) {
    for (InsuranceCard card : cards) {
        if (card.getCardNumber().equals(cardNumber)) {
            return card;
        }   
    }
    return null;
    }
    public Claim getClaimById(String id) {
    for (Claim claim : claims) {
        if (claim.getId().equals(id)) {
            return claim;
        }
    }
    return null;
    }
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

    public boolean addClaim(Claim claim) {
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
    if (claim.getExamDate().isAfter(claim.getClaimDate())) {
        return false;
    }
    if (!claim.getExamDate().isBefore(card.getExpirationDate())) {
        return false;
    }
    claims.add(claim);
        return true;
    }
    public boolean updateClaimStatus(String claimId, String newStatus) {
    Claim claim = getClaimById(claimId);
    if (claim == null) {
        return false;
    }
    if (!Validator.isValidStatus(newStatus)) {
        return false;
    }
    int currentRank = statusRank(claim.getStatus());
    int newRank = statusRank(newStatus);
    if (newRank <= currentRank) {
        return false;
    }
    claim.setStatus(newStatus);
    return true;
   }

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

    private int statusRank(String status) {
    if (status == null) return -1;
    switch (status) {
        case "New": return 0;
        case "Processing": return 1;
        case "Done": return 2;
        default: return -1;
    }
    }
    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public ArrayList<InsuranceCard> getAllCards() {
        return new ArrayList<>(cards);
    }

    public ArrayList<Claim> getAllClaims() {
        return new ArrayList<>(claims);
    }

    public boolean deleteCustomer(String id) {
        Customer customer = getCustomerById(id);
        if (customer == null) {
            return false;
        }
        customers.remove(customer);
        return true;
    }

    public boolean deleteInsuranceCard(String cardNumber) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) {
            return false;
        }
        cards.remove(card);
        return true;
    }

    public boolean deleteClaim(String id) {
        Claim claim = getClaimById(id);
        if (claim == null) {
            return false;
        }
        claims.remove(claim);
        return true;
    }
    public void saveCustomersToFile(String filePath) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
        for (Customer customer : customers) {
            writer.println(customer.toFileString());
        }
    } catch (IOException e) {
        System.out.println("Error saving customers: " + e.getMessage());
    }
    }
    public void loadCustomersFromFile(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                String[] parts = line.split(",");
                String id = parts[0];
                String fullName = parts[1];
                String customerType = parts[2];
                String parentId = parts[3].equals("null") ? null : parts[3];
                Customer customer = new Customer(id, fullName, customerType, parentId);
                customers.add(customer);
            } catch (Exception e) {
                System.out.println("Skipping invalid customer line: " + line);
            }
        }
    } catch (IOException e) {
        System.out.println("Error loading customers: " + e.getMessage());
    }
    }
}