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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository responsible for claim entity management, status workflow
 * enforcement,
 * query filtering, and persistence to claims.txt.
 */
public class ClaimRepository implements ClaimManageable {
    private ArrayList<Claim> claims;
    private CustomerRepository customerRepository;
    private CardRepository cardRepository;

    public ClaimRepository() {
        this.claims = new ArrayList<>();
    }

    public ClaimRepository(CustomerRepository customerRepository, CardRepository cardRepository) {
        this.claims = new ArrayList<>();
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
    }

    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void setCardRepository(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public boolean add(Claim claim) throws InvalidClaimDateException {
        if (claim == null) {
            return false;
        }
        if (!Validator.isValidClaimId(claim.getId())) {
            return false;
        }
        if (getById(claim.getId()) != null) {
            return false;
        }
        if (customerRepository != null && customerRepository.getById(claim.getInsuredPersonId()) == null) {
            return false;
        }
        InsuranceCard card = (cardRepository != null) ? cardRepository.getById(claim.getCardNumber()) : null;
        if (cardRepository != null && card == null) {
            return false;
        }
        if (!Validator.isPositiveAmount(claim.getClaimAmount())) {
            return false;
        }
        if (!Validator.isValidStatus(claim.getStatus())) {
            return false;
        }
        if (claim.getExamDate().toLocalDate().isAfter(claim.getClaimDate().toLocalDate())) {
            throw new InvalidClaimDateException(
                    "Exam date (" + claim.getExamDate().toLocalDate()
                            + ") must be on or before claim date (" + claim.getClaimDate().toLocalDate() + ").");
        }
        if (card != null && !claim.getExamDate().isBefore(card.getExpirationDate())) {
            throw new InvalidClaimDateException(
                    "Exam date (" + claim.getExamDate()
                            + ") must be strictly before insurance card expiration date (" + card.getExpirationDate()
                            + ").");
        }
        claims.add(claim);
        AuditLogger.log(AppContext.getCurrentActorId(), "CREATE_CLAIM", claim.getId());
        return true;
    }

    @Override
    public boolean update(Claim claim) throws InvalidStatusTransitionException {
        if (claim == null) {
            return false;
        }
        for (int i = 0; i < claims.size(); i++) {
            if (claims.get(i).getId().equals(claim.getId())) {
                Claim existing = claims.get(i);
                if (existing.getStatus() != claim.getStatus()) {
                    updateClaimStatus(claim.getId(), claim.getStatus());
                }
                claims.set(i, claim);
                AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_CLAIM", claim.getId());
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a claim's status, enforcing that status can only move
     * forward (NEW -> PROCESSING -> DONE), and is immutable once DONE.
     *
     * @param claimId   the ID of the claim to update
     * @param newStatus the target status
     * @return true if updated successfully, false if claim not found or input is
     *         null
     * @throws InvalidStatusTransitionException if an invalid or backward status
     *                                          transition is attempted
     */
    public boolean updateClaimStatus(String claimId, ClaimStatus newStatus) throws InvalidStatusTransitionException {
        Claim claim = getById(claimId);
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

        // When a claim transitions to DONE (approved), add its amount to the customer's totalClaimAmount
        if (newStatus == ClaimStatus.DONE && customerRepository != null) {
            Customer customer = customerRepository.getById(claim.getInsuredPersonId());
            if (customer != null) {
                customer.setTotalClaimAmount(customer.getTotalClaimAmount() + claim.getClaimAmount());
            }
        }

        AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_CLAIM_STATUS_" + newStatus, claimId);
        return true;
    }

    /**
     * Updates a claim's status using a string representation.
     *
     * @param claimId   the ID of the claim to update
     * @param newStatus the target status string
     * @return true if updated successfully, false if claim not found or status
     *         string is invalid
     * @throws InvalidStatusTransitionException if an invalid or backward status
     *                                          transition is attempted
     */
    public boolean updateClaimStatus(String claimId, String newStatus) throws InvalidStatusTransitionException {
        ClaimStatus status = ClaimStatus.fromString(newStatus);
        if (status == null) {
            return false;
        }
        return updateClaimStatus(claimId, status);
    }

    /**
     * Adds a document to a claim after validating the document name.
     *
     * @param claimId      the ID of the claim
     * @param documentName the document file name
     * @return true if added successfully, false if validation fails
     */
    public boolean addDocument(String claimId, String documentName) {
        Claim claim = getById(claimId);
        if (claim == null) {
            return false;
        }
        if (!Validator.isValidDocumentName(documentName, claim.getId(), claim.getCardNumber())) {
            return false;
        }
        claim.addDocument(documentName);
        AuditLogger.log(AppContext.getCurrentActorId(), "ADD_DOCUMENT_TO_CLAIM", claimId);
        return true;
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        Claim target = getById(id);
        if (target != null) {
            claims.remove(target);
            AuditLogger.log(AppContext.getCurrentActorId(), "DELETE_CLAIM", id);
            return true;
        }
        return false;
    }

    @Override
    public Claim getById(String id) {
        if (id == null) {
            return null;
        }
        for (Claim claim : claims) {
            if (claim.getId().equals(id)) {
                return claim;
            }
        }
        return null;
    }

    @Override
    public List<Claim> getAll() {
        return new ArrayList<>(claims);
    }

    @Override
    public List<Claim> filterByStatus(ClaimStatus status) {
        List<Claim> result = new ArrayList<>();
        if (status == null) {
            return result;
        }
        for (Claim c : claims) {
            if (c.getStatus() == status) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public List<Claim> filterByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Claim> result = new ArrayList<>();
        if (start == null || end == null) {
            return result;
        }
        for (Claim c : claims) {
            LocalDateTime d = c.getClaimDate();
            if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public List<Claim> filterByPolicyHolderFamily(String policyHolderId) {
        List<Claim> result = new ArrayList<>();
        if (policyHolderId == null) {
            return result;
        }

        Set<String> familyCustomerIds = new HashSet<>();
        familyCustomerIds.add(policyHolderId);

        if (customerRepository != null) {
            Customer customer = customerRepository.getById(policyHolderId);
            if (customer instanceof PolicyHolder) {
                PolicyHolder ph = (PolicyHolder) customer;
                for (Dependent dep : ph.getDependents()) {
                    familyCustomerIds.add(dep.getId());
                }
            }
        }

        for (Claim c : claims) {
            if (familyCustomerIds.contains(c.getInsuredPersonId())) {
                result.add(c);
            }
        }
        return result;
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

    /**
     * Loads claims from claims.txt.
     * Uses line.split(",", -1) to preserve trailing empty document columns.
     *
     * @param filePath the path to claims.txt
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

                    if (!Validator.isValidClaimId(claim.getId())
                            || getById(claim.getId()) != null
                            || !Validator.isPositiveAmount(claim.getClaimAmount())
                            || !Validator.isValidStatus(claim.getStatus())) {
                        System.out.println("Skipping invalid claim line: " + line);
                        continue;
                    }
                    claims.add(claim);

                    if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                        String[] documents = parts[7].trim().split("\\|");
                        for (String document : documents) {
                            String documentName = document.trim();
                            if (Validator.isValidDocumentName(documentName, claim.getId(), claim.getCardNumber())) {
                                claim.addDocument(documentName);
                            } else {
                                System.out.println("Skipping invalid document: " + documentName);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Skipping invalid claim line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading claims: " + e.getMessage());
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
}
