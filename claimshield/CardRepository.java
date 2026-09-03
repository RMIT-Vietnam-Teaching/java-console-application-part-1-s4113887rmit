package claimshield;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Repository responsible for insurance card entity management, CRUD operations,
 * search filtering, and persistence to cards.txt.
 */
public class CardRepository implements CardManageable {
    private ArrayList<InsuranceCard> cards;
    private ClaimRepository claimRepository;

    public CardRepository() {
        this.cards = new ArrayList<>();
    }

    public CardRepository(ClaimRepository claimRepository) {
        this.cards = new ArrayList<>();
        this.claimRepository = claimRepository;
    }

    public void setClaimRepository(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public boolean add(InsuranceCard card) {
        if (card == null) {
            return false;
        }
        if (!Validator.isValidCardNumber(card.getCardNumber())) {
            return false;
        }
        if (getById(card.getCardNumber()) != null) {
            return false;
        }
        if (!Validator.isValidCustomerId(card.getCardHolderId())) {
            return false;
        }
        if (!Validator.isValidCustomerId(card.getPolicyOwnerId())) {
            return false;
        }
        cards.add(card);
        AuditLogger.log(AppContext.getCurrentActorId(), "CREATE_INSURANCE_CARD", card.getCardNumber());
        return true;
    }

    @Override
    public boolean update(InsuranceCard card) {
        if (card == null) {
            return false;
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getCardNumber().equals(card.getCardNumber())) {
                cards.set(i, card);
                AuditLogger.log(AppContext.getCurrentActorId(), "UPDATE_INSURANCE_CARD", card.getCardNumber());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        InsuranceCard target = getById(id);
        if (target != null) {
            // Refuse to delete a card that still has claims attached: doing so would
            // orphan those claims (they would reference a card that no longer exists).
            // Removal is only permitted once no claims reference the card.
            if (hasClaimsForCard(id)) {
                return false;
            }
            cards.remove(target);
            AuditLogger.log(AppContext.getCurrentActorId(), "DELETE_INSURANCE_CARD", id);
            return true;
        }
        return false;
    }

    /**
     * Reports whether any claim currently references the given card number.
     * Used as a guard before card deletion so that removing a card cannot leave
     * orphaned claims pointing at a card that no longer exists.
     *
     * @param cardNumber the card number to check
     * @return true if at least one claim references the card, false otherwise
     */
    public boolean hasClaimsForCard(String cardNumber) {
        if (cardNumber == null || claimRepository == null) {
            return false;
        }
        return !claimRepository.filterByCardNumber(cardNumber).isEmpty();
    }

    @Override
    public InsuranceCard getById(String id) {
        if (id == null) {
            return null;
        }
        for (InsuranceCard card : cards) {
            if (card.getCardNumber().equals(id)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Finds an insurance card associated with a specific card holder ID.
     *
     * @param cardHolderId the customer ID of the card holder
     * @return the matching InsuranceCard, or null if not found
     */
    @Override
    public InsuranceCard getByCardHolderId(String cardHolderId) {
        if (cardHolderId == null) {
            return null;
        }
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(cardHolderId)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Finds all insurance cards paid for by a specific policy owner. For a family
     * plan this includes both the owner's own card and the cards issued to their
     * dependents, because every dependent card records the covering PolicyHolder
     * in its policyOwnerId field.
     *
     * @param policyOwnerId the customer ID of the policy owner
     * @return a List of insurance cards funded by that policy owner
     */
    @Override
    public List<InsuranceCard> getByPolicyOwnerId(String policyOwnerId) {
        List<InsuranceCard> result = new ArrayList<>();
        if (policyOwnerId == null) {
            return result;
        }
        for (InsuranceCard card : cards) {
            if (policyOwnerId.equals(card.getPolicyOwnerId())) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * Finds all insurance cards that expire strictly before a given instant,
     * supporting expiry reporting and renewal campaigns.
     *
     * @param deadline the exclusive upper bound on the expiration date
     * @return a List of insurance cards expiring before the deadline
     */
    @Override
    public List<InsuranceCard> filterExpiringBefore(LocalDateTime deadline) {
        List<InsuranceCard> result = new ArrayList<>();
        if (deadline == null) {
            return result;
        }
        for (InsuranceCard card : cards) {
            if (card.getExpirationDate() != null && card.getExpirationDate().isBefore(deadline)) {
                result.add(card);
            }
        }
        return result;
    }

    @Override
    public List<InsuranceCard> getAll() {
        return new ArrayList<>(cards);
    }

    /**
     * Loads insurance cards from cards.txt.
     *
     * @param filePath the path to cards.txt
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

                    if (!Validator.isValidCardNumber(cardNumber)
                            || !Validator.isValidCustomerId(cardHolderId)
                            || !Validator.isValidCustomerId(policyOwnerId)
                            || getById(cardNumber) != null) {
                        System.out.println("Skipping invalid card line: " + line);
                    } else {
                        cards.add(card);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping invalid card line: " + line + " (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading cards: " + e.getMessage());
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
}
